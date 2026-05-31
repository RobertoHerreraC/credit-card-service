package com.bank.creditcard.service;

import com.bank.creditcard.api.dto.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface CreditCardService {
    Single<CreditCardResponse> create(CreditCardRequest request);
    Flowable<CreditCardResponse> findAll();
    Single<CreditCardResponse> findById(String id);
    Single<CreditCardResponse> update(String id, CreditCardRequest request);
    Completable delete(String id);
    Single<CreditCardResponse> charge(String id, CreditCardChargeRequest request);
    Single<CreditCardResponse> pay(String id, CreditCardPaymentRequest request);
    Single<CreditCardBalanceResponse> getBalance(String id);
    Flowable<CreditCardResponse> findByCustomerId(String customerId);
}
