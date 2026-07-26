package com.paypay.learn.ledger;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;

@Service
public class AuthService {

  private final SecurityProperties securityProperties;

  private final AdminProperties adminProperties;

  private final PasswordEncoder passwordEncoder;

  public AuthService(
    PasswordEncoder passwordEncoder,
    SecurityProperties securityProperties,
    AdminProperties adminProperties
  ) {
    this.passwordEncoder = passwordEncoder;
    this.securityProperties = securityProperties;
    this.adminProperties = adminProperties;
  }
  
  public String generateToken(String email, List<String> roles) {
    Algorithm algo = Algorithm.HMAC256(securityProperties.knownSecret());
    return JWT.create()
      .withIssuer(securityProperties.knownIssuer())
      .withSubject(email)
      .withClaim("scope", roles)
      .withIssuedAt(new Date())
      .withExpiresAt(new Date(
        System.currentTimeMillis() + 3600_000
      ))
      .sign(algo)
    ;
  }

  // TODO: Remove hardcoded credentials from SecurityProperties and fetch directly from MySQL database (users table)
  // 1. Define a 'users' table schema in a Flyway migration (e.g., V3__create_users_table.sql)
  // 2. Create UserEntity mapped via JPA and a Spring Data JpaUserRepository
  // 3. Implement BCrypt password hashing when registering/saving a user
  // 4. Query JpaUserRepository by request.email() and check passwordEncoder.matches()
  public Optional<LoginResponse> loginUser(LoginRequest request) {

    List<String> roles = List.of();
    LoginResponse response;

    if (
      request.email().equals(adminProperties.email())
      && passwordEncoder
          .matches(request.password(), adminProperties.passwordHash())
    ) {
      roles = List.of("ADMIN", "USER");
      response = new LoginResponse(
        request.email(),
        generateToken(request.email(), roles),
        roles
      );
    }
    else if (
      request.email().equals(securityProperties.knownEmail())
      && passwordEncoder
        .matches(request.password(), securityProperties.knownPasswordHash())
    ) {
      roles = List.of("USER");
      response = new LoginResponse(
        request.email(),
        generateToken(request.email(), roles),
        roles
      );
    }
    else {
      response = null;
    }
    return Optional.ofNullable(response);
  }
}
