CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    user_name VARCHAR(100),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL
);
