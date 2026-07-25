package com.paypay.learn.ledger;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;


@Profile("mysql")
@Repository
public class JpaIdempotencyRepository implements IdempotencyRepository {

    private final SpringDataIdempotencyRepository springDataRepo;

    public JpaIdempotencyRepository(
        SpringDataIdempotencyRepository springDataRepo
    ) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<IdempotencyEntry> findById(String id) {
      return springDataRepo.findById(id)
        .map(entity -> entity.toDomain())
      ;
    }
    
    @Override
    @Transactional
    /* Clean Rollbacks: 
    If a duplicate key error or database error
    occurs during save(), Spring's transaction
    manager cleanly rolls back the transaction.
    */
    public void save(IdempotencyEntry entry) {
        IdempotencyEntity entity = IdempotencyEntity.fromDomain(entry);
        springDataRepo.save(entity);
    }

}
