package com.paypay.learn.ledger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledger.admin")
public record AdminProperties (
    String email,
    String passwordHash
) {}
