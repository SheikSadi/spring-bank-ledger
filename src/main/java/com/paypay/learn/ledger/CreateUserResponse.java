package com.paypay.learn.ledger;

public record CreateUserResponse(
    String email,
    String role
) {

    public static CreateUserResponse from(User user) {
        return new CreateUserResponse(
            user.email(),
            user.role()
        );
    }

}
