package com.paypay.learn.ledger;

import java.util.List;

public record LoginResponse(
  String email,
  String token,
  List<String> roles
) {
  public LoginResponse {
    if (roles == null || roles.size() < 1) {
      throw new IllegalArgumentException(
        "No roles: the user must have at least 1 valid role."
      );
    }
  }
}
