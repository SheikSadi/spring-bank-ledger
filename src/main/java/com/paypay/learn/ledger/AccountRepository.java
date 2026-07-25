package com.paypay.learn.ledger;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
  Account save(Account acc);
  Optional<Account> delete(String accountId);
  Optional<Account> find(String accountId);
  Optional<Account> findWithWriteLock(String accountId);
  List<Account> findAll();
  List<Account> filterByCurrency(String currency);
}
