package com.paypay.learn.ledger;

import org.springframework.stereotype.Component;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;


@Component
public class LedgerConfigHealthIndicator implements HealthIndicator {
  private final LedgerProperties props;

  public LedgerConfigHealthIndicator(
    LedgerProperties props
  ) {
    this.props = props;
  }

  @Override
  public Health health() {
    String defaultCurrency = props.defaultCurrency();
    if (defaultCurrency == null || defaultCurrency.isBlank()) {
      return Health.down()
        .withDetail(
          "reason", "Default currency was not set in LedgerProperties"
        )
        .build()
      ;
    }
    return Health.up()
      .withDetail(
        "defaultCurrency", defaultCurrency
      )
      .build()
    ;
  }

}
