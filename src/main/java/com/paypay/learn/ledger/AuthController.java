package com.paypay.learn.ledger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthController {

  private final AuthService authService;

  public AuthController(
    AuthService authService
  ) {
    this.authService = authService;
  }

  @PostMapping("/auth/login")
  ResponseEntity<?> loginUser(
    @RequestBody LoginRequest request
  ) throws InvalidLoginException {
    LoginResponse body = authService.loginUser(request)
      .orElseThrow(
        () -> new InvalidLoginException()
      )
    ;
    return ResponseEntity.ok().body(body);
  }
}
