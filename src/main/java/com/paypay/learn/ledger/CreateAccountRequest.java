package com.paypay.learn.ledger;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountRequest(
    @NotBlank @Size(min = 3, max = 100) String owner,
    @Pattern(regexp = "^[A-Z]{3}$") String currency,
    @NotNull @PositiveOrZero BigDecimal initialBalance
) {}
