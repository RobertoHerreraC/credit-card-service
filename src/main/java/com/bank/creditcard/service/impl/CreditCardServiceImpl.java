package com.bank.creditcard.service.impl;

import com.bank.creditcard.api.dto.*;
import com.bank.creditcard.client.CustomerClient;
import com.bank.creditcard.client.dto.CustomerResponse;
import com.bank.creditcard.domain.CreditCard;
import com.bank.creditcard.domain.CreditCardType;
import com.bank.creditcard.domain.CustomerType;
import com.bank.creditcard.exception.BusinessRuleException;
import com.bank.creditcard.exception.CreditCardNotFoundException;
import com.bank.creditcard.exception.CustomerNotFoundException;
import com.bank.creditcard.mapper.CreditCardMapper;
import com.bank.creditcard.repository.CreditCardRepository;
import com.bank.creditcard.service.CreditCardService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {

//    private static final String PERSONAL = "PERSONAL";
//    private static final String BUSINESS = "BUSINESS";

    private final CreditCardRepository creditCardRepository;
    private final CreditCardMapper creditCardMapper;
    private final CustomerClient customerClient;

    @Override
    public Single<CreditCardResponse> create(CreditCardRequest request) {
        log.info("Creating credit card for customerId: {}", request.getCustomerId());

        CreditCardType cardType = parseCardType(request.getCardType());
        validateCreditLimit(request);
        return findCustomerOrFail(request.getCustomerId())
                .map(customer -> {
                    validateCustomerCardType(customer, cardType);
                    return creditCardMapper.toEntity(request);
                })
                .flatMap(creditCard ->
                        Single.fromPublisher(creditCardRepository.save(creditCard)))
                .map(creditCardMapper::toResponse)
                .doOnSuccess(response ->
                        log.info("Credit card created successfully with id: {}", response.getId()));
    }

    @Override
    public Flowable<CreditCardResponse> findAll() {
        log.info("Finding all active credit cards");
        return Flowable.fromPublisher(creditCardRepository.findByActiveTrue())
                .map(creditCardMapper::toResponse);
    }

    @Override
    public Single<CreditCardResponse> findById(String id) {
        log.info("Finding credit card by id: {}", id);
        return Single.fromPublisher(
                        creditCardRepository.findByIdAndActiveTrue(id)
                                .switchIfEmpty(Mono.error(new CreditCardNotFoundException(id)))
                )
                .map(creditCardMapper::toResponse);
    }

    @Override
    public Single<CreditCardResponse> update(String id, CreditCardRequest request) {
        log.info("Updating credit card with id: {}", id);

        CreditCardType cardType = parseCardType(request.getCardType());
        validateCreditLimit(request);
        return Single.fromPublisher(
                        creditCardRepository.findByIdAndActiveTrue(id)
                                .switchIfEmpty(Mono.error(new CreditCardNotFoundException(id)))
                )
                .flatMap(existingCard ->
                        findCustomerOrFail(request.getCustomerId())
                                .map(customer -> {
                                    validateCustomerCardType(customer, cardType);
                                    validateNewLimit(existingCard, request);
                                    return creditCardMapper.updateEntity(existingCard, request);
                                })
                )
                .flatMap(updatedCard ->
                        Single.fromPublisher(creditCardRepository.save(updatedCard)))
                .map(creditCardMapper::toResponse)
                .doOnSuccess(response ->
                        log.info("Credit card updated successfully with id: {}", response.getId()));
    }

    @Override
    public Completable delete(String id) {
        log.info("Deleting credit card logically with id: {}", id);
        return Single.fromPublisher(
                        creditCardRepository.findByIdAndActiveTrue(id)
                                .switchIfEmpty(Mono.error(new CreditCardNotFoundException(id)))
                )
                .map(creditCard -> {
                    creditCard.setActive(Boolean.FALSE);
                    return creditCard;
                })
                .flatMap(creditCard ->
                        Single.fromPublisher(creditCardRepository.save(creditCard)))
                .ignoreElement();
    }

    @Override
    public Single<CreditCardResponse> charge(String id, CreditCardChargeRequest request) {
        log.info("Charging credit card with id: {}", id);

        BigDecimal chargeAmount = validateChargeAmount(request);
        return Single.fromPublisher(
                        creditCardRepository.findByIdAndActiveTrue(id)
                                .switchIfEmpty(Mono.error(new CreditCardNotFoundException(id)))
                )
                .map(creditCard -> applyCharge(creditCard, chargeAmount))
                .flatMap(creditCard ->
                        Single.fromPublisher(creditCardRepository.save(creditCard)))
                .map(creditCardMapper::toResponse)
                .doOnSuccess(response ->
                        log.info("Charge registered successfully for credit card id: {}", id));
    }

    @Override
    public Single<CreditCardResponse> pay(String id, CreditCardPaymentRequest request) {
        log.info("Paying credit card with id: {}", id);

        BigDecimal paymentAmount = validatePaymentAmount(request);
        return Single.fromPublisher(
                        creditCardRepository.findByIdAndActiveTrue(id)
                                .switchIfEmpty(Mono.error(new CreditCardNotFoundException(id)))
                )
                .map(creditCard -> applyPayment(creditCard, paymentAmount))
                .flatMap(creditCard ->
                        Single.fromPublisher(creditCardRepository.save(creditCard)))
                .map(creditCardMapper::toResponse)
                .doOnSuccess(response ->
                        log.info("Payment registered successfully for credit card id: {}", id));
    }

    @Override
    public Single<CreditCardBalanceResponse> getBalance(String id) {
        log.info("Getting credit card balance for id: {}", id);
        return Single.fromPublisher(
                        creditCardRepository.findByIdAndActiveTrue(id)
                                .switchIfEmpty(Mono.error(new CreditCardNotFoundException(id)))
                )
                .map(creditCardMapper::toBalanceResponse)
                .doOnSuccess(response ->
                        log.info("Credit card balance found successfully for id: {}", id));
    }

    @Override
    public Flowable<CreditCardResponse> findByCustomerId(String customerId) {
        log.info("Finding credit cards by customer id: {}", customerId);
        return Flowable.fromPublisher(
                        customerClient.findCustomerById(customerId)
                                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerId)))
                                .thenMany(creditCardRepository.findByCustomerIdAndActiveTrue(customerId))
                )
                .map(creditCardMapper::toResponse)
                .doOnComplete(() ->
                        log.info("Credit cards found successfully for customer id: {}", customerId));
    }

    private Single<CustomerResponse> findCustomerOrFail(String customerId) {
        return Single.fromPublisher(
                customerClient.findCustomerById(customerId)
                        .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerId)))
                        .onErrorMap(error -> new CustomerNotFoundException(customerId))
        );
    }

    private void validateCustomerCardType(
            CustomerResponse customer,
            CreditCardType cardType) {
        log.info(CustomerType.BUSINESS.name());
        log.info(CustomerType.BUSINESS.toString());
        if (CustomerType.PERSONAL.name().equals(customer.getCustomerType())
                && CreditCardType.BUSINESS.equals(cardType)) {
            throw new BusinessRuleException(
                    "Personal customers cannot create business credit cards"
            );
        }

        if (CustomerType.BUSINESS.name().equals(customer.getCustomerType())
                && CreditCardType.PERSONAL.equals(cardType)) {
            throw new BusinessRuleException(
                    "Business customers cannot create personal credit cards"
            );
        }
    }

    private void validateCreditLimit(CreditCardRequest request) {
        BigDecimal creditLimit = BigDecimal.valueOf(request.getCreditLimit());

        if (creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Credit limit must be greater than zero");
        }
    }

    private void validateNewLimit(CreditCard existingCard, CreditCardRequest request) {
        BigDecimal newLimit = BigDecimal.valueOf(request.getCreditLimit());
        BigDecimal usedAmount = defaultBigDecimal(existingCard.getUsedAmount());

        if (newLimit.compareTo(usedAmount) < 0) {
            throw new BusinessRuleException(
                    "Credit limit cannot be less than used amount"
            );
        }
    }

    private BigDecimal validateChargeAmount(CreditCardChargeRequest request) {
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Charge amount must be greater than zero");
        }
        return amount;
    }

    private BigDecimal validatePaymentAmount(CreditCardPaymentRequest request) {
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero");
        }
        return amount;
    }

    private CreditCard applyCharge(CreditCard creditCard, BigDecimal chargeAmount) {
        BigDecimal availableBalance = defaultBigDecimal(creditCard.getAvailableBalance());

        if (chargeAmount.compareTo(availableBalance) > 0) {
            throw new BusinessRuleException("Credit limit exceeded");
        }

        BigDecimal usedAmount = defaultBigDecimal(creditCard.getUsedAmount());

        creditCard.setUsedAmount(usedAmount.add(chargeAmount));
        creditCard.setAvailableBalance(availableBalance.subtract(chargeAmount));
        return creditCard;
    }

    private CreditCard applyPayment(CreditCard creditCard, BigDecimal paymentAmount) {
        BigDecimal usedAmount = defaultBigDecimal(creditCard.getUsedAmount());

        if (paymentAmount.compareTo(usedAmount) > 0) {
            throw new BusinessRuleException(
                    "Payment amount cannot be greater than used amount"
            );
        }

        BigDecimal availableBalance = defaultBigDecimal(creditCard.getAvailableBalance());

        creditCard.setUsedAmount(usedAmount.subtract(paymentAmount));
        creditCard.setAvailableBalance(availableBalance.add(paymentAmount));
        return creditCard;
    }

    private CreditCardType parseCardType(String cardType) {
        try {
            return CreditCardType.valueOf(cardType);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessRuleException("Invalid credit card type: " + cardType);
        }
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}