package com.paypay.learn.ledger;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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

  @GetMapping("/auth/whoami")
  ResponseEntity<?> whoami(JwtAuthenticationToken token) {
    Map<String, Object> response = new HashMap<>();
    response.put("attributes", token.getTokenAttributes());
    response.put("authorities", token.getAuthorities());
    
    return ResponseEntity.ok().body(response);
  }

}
