package com.paypay.learn.ledger;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateUserRequest(
    @Size(min = 3) String userName,
    @Email String email,
    @Size(min = 3) String firstName,
    @Size(min = 3) String lastname,
    @NotBlank String role,
    @NotBlank String password
) {}
