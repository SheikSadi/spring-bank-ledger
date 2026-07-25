package com.paypay.learn.ledger;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;


@Service
public class AccountService {

  private final AccountRepository repo;
  private final LedgerProperties properties;

  public AccountService(AccountRepository repo, LedgerProperties properties) {
    this.repo = repo;
    this.properties = properties;
  }
  
  public Account createAccount(CreateAccountRequest req) {
    String id = UUID.randomUUID().toString();
    String resolvedCurrency = Optional.ofNullable(req.currency()).orElse(properties.defaultCurrency());
    Account acc = new Account(
      id, req.owner(), resolvedCurrency, req.initialBalance()
    );
    return repo.save(acc);
  }

  @Transactional // tells Spring to start a database transaction before entering the method and commit it when the method finishes
  public Account
  updateAccount(String accountId, UpdateAccountRequest req) {
    Optional<Account> maybeAccount = repo.findWithWriteLock(accountId);

    Account existing = maybeAccount
        .orElseThrow(() -> new AccountNotFoundException(accountId)
    );

    Account updated = new Account(
        existing.id(),
        existing.owner(),
        Optional.ofNullable(req.currency()).orElse(existing.currency()),
        Optional.ofNullable(req.balance()).orElse(existing.balance())
    );
    repo.save(updated);
    return updated;
  }

  @Transactional
  public Account creditAccount(String accountId, BigDecimal balance) {
    Optional<Account> maybeAccount = repo.findWithWriteLock(accountId);
    Account existing = maybeAccount
      .orElseThrow(() -> new AccountNotFoundException(accountId))
    ;
    Account updated = new Account(
      existing.id(),
      existing.owner(),
      existing.currency(),
      existing.balance().add(balance)
    );
    repo.save(updated);
    return updated;
  }

  @Transactional
  public Account debitAccount(String accountId, BigDecimal balance) {
    Optional<Account> maybeAccount = repo.findWithWriteLock(accountId);
    Account existing = maybeAccount
      .orElseThrow(() -> new AccountNotFoundException(accountId))
    ;

    if (existing.balance().compareTo(balance) < 0) {
      throw new InsufficientFundsException(
        existing.balance(), balance
      );
    }

    Account updated = new Account(
      existing.id(),
      existing.owner(),
      existing.currency(),
      existing.balance().subtract(balance)
    );
    repo.save(updated);
    return updated;
  }

  public Boolean deleteAccount(String id) {
    Optional<Account> maybeAccount = repo.delete(id);
    // if (maybeAccount.isEmpty()) {
    //   return false;
    // } else {
    //   return true;
    // }
    return maybeAccount.isPresent();
  }

  public Optional<Account> getAccountById(String id) {
    return repo.find(id);
  }

  public List<Account> getAccounts() {
    return repo.findAll();
  }

  public List<Account> getAccounts(String currency) {
    return repo.filterByCurrency(currency);
  }

}
