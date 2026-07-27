package com.paypay.learn.ledger;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> delete(String id);

    List<User> findAll();

    Optional<User> findById(String id);

    Optional<String> findPasswordHash(String email);

    Optional<String> getRole(String email);

}
