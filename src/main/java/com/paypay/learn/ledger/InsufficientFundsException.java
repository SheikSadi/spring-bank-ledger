package com.paypay.learn.ledger;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(
      BigDecimal currentBalance, BigDecimal requestedDebit 
    ) {
        super(
          String.format("The requested debit %s exceeds the current balance %s", requestedDebit, currentBalance)
        );
    }
}
