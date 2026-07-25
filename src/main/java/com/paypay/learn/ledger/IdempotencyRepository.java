package com.paypay.learn.ledger;

import java.util.Optional;

public interface IdempotencyRepository {
    public Optional<IdempotencyEntry> findById(String id);
    public void save(IdempotencyEntry entry);
}
