package com.paypay.learn.ledger;

public class IdempotencyKeyNotProvidedException extends Exception {
    public IdempotencyKeyNotProvidedException() {
        super("Invalid request: Must provide 'Idempotency-Key' header.");
    }
}
