package com.paypay.learn.ledger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledger")
public record LedgerProperties(
    String defaultCurrency
) {}
