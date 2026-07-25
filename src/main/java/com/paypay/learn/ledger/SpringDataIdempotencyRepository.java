package com.paypay.learn.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyEntity, String> {}
