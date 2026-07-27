package com.paypay.learn.ledger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledger.test-user")
public record TestUserProperties(
    String email,
    String password
) {}
