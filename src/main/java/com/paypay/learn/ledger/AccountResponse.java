package com.paypay.learn.ledger;

import java.math.BigDecimal;

public record AccountResponse(
    String id,
    String owner,
    String currency,
    BigDecimal balance
) {
    public static AccountResponse from(Account acc){
        return new AccountResponse(
            acc.id(), acc.owner(), acc.currency(), acc.balance()
        );
    }
}
