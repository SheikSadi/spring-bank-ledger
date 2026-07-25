package com.paypay.learn.ledger;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("in-memory")
@Repository
public class InMemoryAccountRepository implements AccountRepository {

  private final Map<String, Account> store = new ConcurrentHashMap<String, Account>();

  @Override
  public Account save(Account acc) {
    store.put(acc.id(), acc);
    return acc;
  }

  @Override
  public Optional<Account> delete(String accountId) {
    return Optional.ofNullable(store.remove(accountId));
  }

  @Override
  public Optional<Account> find(String accountId) {
    return Optional.ofNullable(store.get(accountId));
  }
 
  @Override
  public Optional<Account> findWithWriteLock(String accountId) {
    return find(accountId);
  }

  @Override
  public List<Account> findAll() {
    return store.values().stream().toList();
  }

  @Override
  public List<Account> filterByCurrency(String currency) {
    return store.values().stream()
        .filter(acc -> currency.equals(acc.currency()))
        .toList();
  }
}
