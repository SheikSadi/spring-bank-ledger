package com.paypay.learn.ledger;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private int statusCode;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // No arg constructor required by JPA
    public IdempotencyEntity() {}

    public IdempotencyEntity(
        String id,
        Integer statusCode,
        String responseBody,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
    }

    public IdempotencyEntry toDomain() {
        return new IdempotencyEntry(
            this.id,
            this.statusCode,
            this.responseBody,
            this.createdAt
        );
    }

    public static IdempotencyEntity fromDomain(IdempotencyEntry domain) {
        return new IdempotencyEntity(
            domain.id(),
            domain.statusCode(),
            domain.responseBody(),
            domain.createdAt()
        );
    }
}
