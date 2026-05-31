package com.bank.creditcard.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "credit_cards")
public class CreditCard {
    @Id
    private String id;
    private String customerId;
    private String cardNumber;
    private CreditCardType cardType;
    private BigDecimal creditLimit;
    private BigDecimal usedAmount;
    private BigDecimal availableBalance;
    private Boolean active;
}