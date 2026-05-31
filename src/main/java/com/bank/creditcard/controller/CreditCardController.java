package com.bank.creditcard.controller;

import com.bank.creditcard.api.dto.*;
import com.bank.creditcard.api.generated.CreditCardsApi;
import com.bank.creditcard.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CreditCardController implements CreditCardsApi {

    private final CreditCardService creditCardService;

    @Override
    public Mono<ResponseEntity<CreditCardResponse>> createCreditCard(
            @Valid Mono<CreditCardRequest> creditCardRequest,
            ServerWebExchange exchange) {
        return creditCardRequest
                .flatMap(request ->
                        Mono.fromCompletionStage(
                                creditCardService.create(request).toCompletionStage()
                        )
                )
                .map(response ->
                        ResponseEntity.status(HttpStatus.CREATED).body(response)
                );
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditCardResponse>>> findAllCreditCards(
            ServerWebExchange exchange) {
        return Mono.just(
                ResponseEntity.ok(
                        Flux.from(creditCardService.findAll())
                )
        );
    }

    @Override
    public Mono<ResponseEntity<CreditCardResponse>> findCreditCardById(
            String id,
            ServerWebExchange exchange) {
        return Mono.fromCompletionStage(
                        creditCardService.findById(id).toCompletionStage()
                )
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CreditCardResponse>> updateCreditCard(
            String id,
            @Valid Mono<CreditCardRequest> creditCardRequest,
            ServerWebExchange exchange) {
        return creditCardRequest
                .flatMap(request ->
                        Mono.fromCompletionStage(
                                creditCardService.update(id, request).toCompletionStage()
                        )
                )
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCreditCard(
            String id,
            ServerWebExchange exchange) {
        return Mono.fromCompletionStage(
                        creditCardService.delete(id).toCompletionStage(null)
                )
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<CreditCardResponse>> chargeCreditCard(
            String id,
            @Valid Mono<CreditCardChargeRequest> creditCardChargeRequest,
            ServerWebExchange exchange) {
        return creditCardChargeRequest
                .flatMap(request ->
                        Mono.fromCompletionStage(
                                creditCardService.charge(id, request).toCompletionStage()
                        )
                )
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CreditCardResponse>> payCreditCard(
            String id,
            @Valid Mono<CreditCardPaymentRequest> creditCardPaymentRequest,
            ServerWebExchange exchange) {
        return creditCardPaymentRequest
                .flatMap(request ->
                        Mono.fromCompletionStage(
                                creditCardService.pay(id, request).toCompletionStage()
                        )
                )
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CreditCardBalanceResponse>> getCreditCardBalance(
            String id,
            ServerWebExchange exchange) {
        return Mono.fromCompletionStage(
                        creditCardService.getBalance(id).toCompletionStage()
                )
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditCardResponse>>> findCreditCardsByCustomerId(
            String customerId,
            ServerWebExchange exchange) {
        return Mono.just(
                ResponseEntity.ok(
                        Flux.from(creditCardService.findByCustomerId(customerId))
                )
        );
    }
}
