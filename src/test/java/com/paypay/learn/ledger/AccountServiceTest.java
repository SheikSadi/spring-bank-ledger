package com.paypay.learn.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
public class AccountServiceTest {

  @Autowired
  private AccountService service;

  @Test
  void negativeDebitShouldThrow() {
    Account created = service.createAccount(new CreateAccountRequest(
      "Sadi", "JPY", BigDecimal.valueOf(100)
    ));

    assertThatThrownBy(() -> {
      // Debit more that existing balance
      service.debitAccount(created.id(), BigDecimal.valueOf(500));
    })
      .isInstanceOf(InsufficientFundsException.class)
      .hasMessageContaining("exceeds the current balance")
    ;
  }

  @Nested
  @ActiveProfiles("mysql")
  class ConcurrencyTests {

    @Test
    void shouldHandleConcurrentUpdatesSafely() throws InterruptedException {
      Account created = service.createAccount(
        new CreateAccountRequest("Sadi", "JPY", BigDecimal.valueOf(1000))
      );
      TestUtils.runConcurrently(
        2,
        () -> service.debitAccount(created.id(), BigDecimal.valueOf(100))
      );

      Account updated = service.getAccountById(created.id())
        .orElseThrow(() -> new RuntimeException("Expected the account to exist: " + created.id()))
      ;

      assertThat(updated.balance())
        .isEqualByComparingTo(BigDecimal.valueOf(800))
      ;

    }
  }

}
