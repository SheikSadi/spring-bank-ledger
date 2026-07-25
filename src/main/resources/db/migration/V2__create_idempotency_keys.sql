CREATE TABLE idempotency_keys (
    id VARCHAR(255) PRIMARY KEY,
    status_code INT NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
