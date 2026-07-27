package com.paypay.learn.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User createUser(CreateUserRequest request) {
        return repo.save(User.from(request));
    }

    public List<User> getUsers() {
        return repo.findAll();
    }

    public Optional<User> findUserById(String id) {
        return repo.findById(id);
    }

    public Optional<User> deleteUser(String id) {
        return repo.delete(id);
    }

    public Optional<String> findPasswordHash(String email) {
        return repo.findPasswordHash(email);
    }

    public Optional<String> getRole(String email) {
        return repo.getRole(email);
    }


}
