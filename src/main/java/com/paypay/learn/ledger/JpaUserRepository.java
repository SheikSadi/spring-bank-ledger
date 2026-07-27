package com.paypay.learn.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;


@Profile("mysql")
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataRepo;

    public JpaUserRepository(
        SpringDataUserRepository springDataRepo
    ) {
        this.springDataRepo = springDataRepo;
    }

    
    
    @Override
    @Transactional
    /* Clean Rollbacks: 
    If a duplicate key error or database error
    occurs during save(), Spring's transaction
    manager cleanly rolls back the transaction.
    */ 
    public User save(User user) {
        springDataRepo.save(UserEntity.fromDomain(user));
        return user;
    }

    @Override
    public Optional<User> delete(String id) {
        Optional<UserEntity> entity = springDataRepo.findById(id);
        entity
            .ifPresent(e -> springDataRepo.delete(e));
        ;
        return entity
            .map(e -> e.toDomain())
        ;
    }

    @Override
    public List<User> findAll() {
        return springDataRepo.findAll().stream()
            .map(e -> e.toDomain())
            .toList()
        ;
    }

    @Override
    public Optional<User> findById(String id) {
        return springDataRepo.findById(id)
            .map(e -> e.toDomain())
        ;
    }

    
    @Override
    public Optional<String> findPasswordHash(String email) {
        Optional<UserEntity> entity = springDataRepo.findByEmail(email).stream()
            .findFirst()
        ;
        return entity
            .map(e -> e.toDomain().passwordHash())
        ;
    }

        
    @Override
    public Optional<String> getRole(String email) {
        Optional<UserEntity> entity = springDataRepo.findByEmail(email).stream()
            .findFirst()
        ;
        return entity
            .map(e -> e.toDomain().role())
        ;
    }


}
