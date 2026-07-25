package com.paypay.learn.ledger;

public record LoginRequest(
  String email,
  String password
) {
  public LoginRequest {
    if (email == null || !email.contains("@")) {
      throw new IllegalArgumentException(
        "Invalid email: cannot create LoginRequest record"
      );
    }
  }
}
