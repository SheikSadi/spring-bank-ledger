package com.paypay.learn.ledger;

import java.time.LocalDateTime;

public record IdempotencyEntry(
    String id,
    int statusCode,
    String responseBody,
    LocalDateTime createdAt
) {

}
