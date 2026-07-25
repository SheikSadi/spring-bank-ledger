package com.paypay.learn.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;


@Profile("mysql")
@Repository
public class JpaAccountRepository implements AccountRepository {

  private final SpringDataAccountRepository jpaRepo;

  public JpaAccountRepository(SpringDataAccountRepository jpaRepo) {
    this.jpaRepo = jpaRepo;
  }

  @Override
  public Account save(Account acc) {
    AccountEntity entity = AccountEntity.fromDomain(acc);
    AccountEntity saved = jpaRepo.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<Account> find(String accountId) {
    return jpaRepo.findById(accountId)
      .map(entity -> entity.toDomain())
    ;
  }

  @Override
  public Optional<Account> findWithWriteLock(String accountId) {
    return jpaRepo.findWithLockById(accountId)
      .map(entity -> entity.toDomain())
    ;
  }

  @Override
  public Optional<Account> delete(String accountId) {
    return jpaRepo.findById(accountId)
      .map(entity -> {
        jpaRepo.delete(entity);
        return entity.toDomain();
      })
    ;
  }
  
  @Override
  public List<Account> findAll() {
    return jpaRepo.findAll().stream()
      .map(entity -> entity.toDomain())
      .toList()
    ;
  }

  @Override
  public List<Account> filterByCurrency(String currency) {
    // Unoptimized version
    // return jpaRepo.findAll().stream()
    //   .map(entity -> entity.toDomain())
    //   .filter(acc -> currency.equals(acc.currency()))
    //   .toList()
    // ;
    return jpaRepo.findByCurrency(currency).stream()
      .map(entity -> entity.toDomain())
      .toList()
    ;
  }
}
