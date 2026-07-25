package com.paypay.learn.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

@SpringBootTest
public class AccountTest {
  @Test
  void negativeBalanceShouldThrow() {
    assertThatThrownBy(() -> {
      new Account("failing-1", "Sadi", "JPY", BigDecimal.valueOf(-10));
    })
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("cannot be negative")
    ;
  }
}
