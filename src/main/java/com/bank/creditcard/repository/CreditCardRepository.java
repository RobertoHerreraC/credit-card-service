package com.bank.creditcard.repository;

import com.bank.creditcard.domain.CreditCard;
import com.bank.creditcard.domain.CreditCardType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CreditCardRepository extends ReactiveMongoRepository<CreditCard, String> {
    Mono<CreditCard> findByCardNumber(String cardNumber);
    Flux<CreditCard> findByCustomerIdAndActiveTrue(String customerId);
    Flux<CreditCard> findByCustomerIdAndCardTypeAndActiveTrue(
            String customerId,
            CreditCardType cardType
    );
    Mono<CreditCard> findByIdAndActiveTrue(String id);
    Flux<CreditCard> findByActiveTrue();
}