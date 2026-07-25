package com.paypay.learn.ledger;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreditAccountRequest(
  @NotNull @Positive BigDecimal amount
) {}
