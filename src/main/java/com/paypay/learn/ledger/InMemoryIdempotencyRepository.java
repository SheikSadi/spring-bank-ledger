package com.paypay.learn.ledger;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;


@Profile({"in-memory","dev", "redis"})
@Repository
public class InMemoryIdempotencyRepository implements IdempotencyRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryAccountRepository.class);

    private final ConcurrentHashMap<String, IdempotencyEntry> repo = new ConcurrentHashMap<String, IdempotencyEntry>();

    @Override
    public Optional<IdempotencyEntry> findById(String id) {
        IdempotencyEntry entry = repo.get(id);
        return Optional.ofNullable(entry);
    }

    @Override
    public void save(IdempotencyEntry entry) {
        repo.put(entry.id(), entry);
        logger.info("Stored idempotency entry with key: {}", entry.id());
    }
}
