package com.paypay.learn.ledger;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public record User(
    String id,
    String userName,
    String email,
    String firstName,
    String lastname,
    String role,
    String passwordHash
) {

    public User {
        if (email == null || role == null) {
            throw new IllegalArgumentException();
        }
    }

    public static User from(CreateUserRequest request) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return new User(
            UUID.randomUUID().toString(),
            request.userName(),
            request.email(),
            request.firstName(),
            request.lastname(),
            request.role(),
            encoder.encode(request.password())
        );
    }

}
