package com.paypay.learn.ledger;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class AccountEntity {

  @Id
  private String id;

  @Column(nullable = false)
  private String owner;

  @Column(nullable = false)
  private String currency;

  @Column(nullable = false, precision = 18, scale = 4)
  private BigDecimal balance;

  // No arg constructor required by JPA
  protected AccountEntity() {}

  public AccountEntity(
    String id,
    String owner,
    String currency,
    BigDecimal balance
  ) {
    this.id = id;
    this.owner = owner;
    this.currency = currency;
    this.balance = balance;
  }

  public Account toDomain() {
    Account domain = new Account(this.id, this.owner, this.currency, this.balance);
    return domain;
  }

  public static AccountEntity fromDomain(Account domain) {
    return new AccountEntity(
      domain.id(),
      domain.owner(),
      domain.currency(),
      domain.balance()
    );
  }
}
