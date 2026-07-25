package com.paypay.learn.ledger;

import java.math.BigDecimal;

public record Account(
    String id,
    String owner,
    String currency,
    BigDecimal balance
) {
    // Compact constructor - no parenthesis
    public Account {
        if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Account balance cannot be negative"
            );
        }
    }
}
