package com.paypay.learn.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, String> {
  List<AccountEntity> findByCurrency(String currency);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  // Spring Data JPA's QUERY DERIVATION
  // ingnores anything between find...By
  // So we can store metadata e.g. WithLock
  Optional<AccountEntity> findWithLockById(String accountId);
}
