package com.bank.creditcard.exception;

public class CreditCardNotFoundException extends RuntimeException {
    public CreditCardNotFoundException(String id) {
        super("Credit card not found with id: " + id);
    }
}