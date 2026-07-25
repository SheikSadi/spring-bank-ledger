package com.paypay.learn.ledger;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    List<FieldErrorDetail> fieldErrors
) {}
