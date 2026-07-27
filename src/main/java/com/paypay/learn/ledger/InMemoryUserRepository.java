package com.paypay.learn.ledger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("in-memory")
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final ConcurrentHashMap<String, User> map = new ConcurrentHashMap<String, User>();

    public InMemoryUserRepository(
        AdminProperties adminProperties,
        TestUserProperties testUserProperties
    ) {
        User admin = User.from(new CreateUserRequest(
            null,
            adminProperties.email(),
            null,
            null,
            "ADMIN",
            adminProperties.password()
        ));
        
        User user = User.from(new CreateUserRequest(
            null,
            testUserProperties.email(),
            null,
            null,
            "USER",
            testUserProperties.password()
        ));

        map.put(admin.id(), admin);
        map.put(user.id(), user);
    }

    @Override
    public User save(User user) {
        map.put(
            user.id(), user
        );
        return user;
    }

    @Override
    public Optional<User> delete(String id) {

        User user = map.get(id);

        if (user != null) {
            map.remove(user.id());
        }

        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        return map.values().stream()
            .toList()
        ;
    }

    @Override
    public Optional<User> findById(String id) {
        User user = map.get(id);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<String> findPasswordHash(String email) {
        List<User> users = map.values().stream()
            .filter(u -> email.equals(u.email()))
            .toList()
        ;

        String passwordHash = users.size() > 0
            ? users.get(0).passwordHash()
            : null
        ;
        
        return Optional.ofNullable(passwordHash);
    }

    @Override
    public Optional<String> getRole(String email) {
        List<User> users = map.values().stream()
            .filter(u -> email.equals(u.email()))
            .toList()
        ;

        String role = users.size() > 0
            ? users.get(0).role()
            : null
        ;
        
        return Optional.ofNullable(role);
    }

}
