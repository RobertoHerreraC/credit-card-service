package com.bank.creditcard.mapper;

import com.bank.creditcard.api.dto.CreditCardBalanceResponse;
import com.bank.creditcard.api.dto.CreditCardRequest;
import com.bank.creditcard.api.dto.CreditCardResponse;
import com.bank.creditcard.domain.CreditCard;
import com.bank.creditcard.domain.CreditCardType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CreditCardMapper {

    public CreditCard toEntity(CreditCardRequest request) {
        BigDecimal creditLimit = toBigDecimal(request.getCreditLimit());
        return CreditCard.builder()
                .customerId(request.getCustomerId())
                .cardNumber(generateCardNumber())
                .cardType(CreditCardType.valueOf(request.getCardType()))
                .creditLimit(creditLimit)
                .usedAmount(BigDecimal.ZERO)
                .availableBalance(creditLimit)
                .active(Boolean.TRUE)
                .build();
    }

    public CreditCardResponse toResponse(CreditCard creditCard) {
        CreditCardResponse response = new CreditCardResponse();

        response.setId(creditCard.getId());
        response.setCustomerId(creditCard.getCustomerId());
        response.setCardNumber(creditCard.getCardNumber());
        response.setCardType(creditCard.getCardType().name());
        response.setCreditLimit(toDouble(creditCard.getCreditLimit()));
        response.setUsedAmount(toDouble(creditCard.getUsedAmount()));
        response.setAvailableBalance(toDouble(creditCard.getAvailableBalance()));
        response.setActive(creditCard.getActive());
        return response;
    }

    public CreditCard updateEntity(CreditCard creditCard, CreditCardRequest request) {
        BigDecimal creditLimit = toBigDecimal(request.getCreditLimit());
        BigDecimal usedAmount = defaultBigDecimal(creditCard.getUsedAmount());

        creditCard.setCustomerId(request.getCustomerId());
        creditCard.setCardType(CreditCardType.valueOf(request.getCardType()));
        creditCard.setCreditLimit(creditLimit);
        creditCard.setAvailableBalance(creditLimit.subtract(usedAmount));
        return creditCard;
    }

    public CreditCardBalanceResponse toBalanceResponse(CreditCard creditCard) {
        CreditCardBalanceResponse response = new CreditCardBalanceResponse();

        response.setCreditCardId(creditCard.getId());
        response.setCardNumber(creditCard.getCardNumber());
        response.setCreditLimit(toDouble(creditCard.getCreditLimit()));
        response.setUsedAmount(toDouble(creditCard.getUsedAmount()));
        response.setAvailableBalance(toDouble(creditCard.getAvailableBalance()));
        return response;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private String generateCardNumber() {
        return "CARD-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}