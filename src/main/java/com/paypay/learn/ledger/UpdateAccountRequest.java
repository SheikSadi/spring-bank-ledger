package com.paypay.learn.ledger;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateAccountRequest(
    @Pattern(regexp = "^[A-Z]{3}$") String currency,
    @PositiveOrZero BigDecimal balance
) {}
