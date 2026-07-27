package com.paypay.learn.ledger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledger.security")
public record SecurityProperties(
  String knownIssuer,
  String knownSecret
) {}
