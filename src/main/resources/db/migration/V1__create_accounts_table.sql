CREATE TABLE accounts (
    id VARCHAR(36) PRIMARY KEY,
    owner VARCHAR(100) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance DECIMAL(18, 4) NOT NULL
);
