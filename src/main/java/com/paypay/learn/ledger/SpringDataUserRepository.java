package com.paypay.learn.ledger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, String> {
    List<UserEntity> findByEmail(String email);
}
