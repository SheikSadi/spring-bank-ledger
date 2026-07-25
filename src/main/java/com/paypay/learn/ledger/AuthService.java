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

  private final SecurityProperties props;

  private final PasswordEncoder passwordEncoder;

  public AuthService(
    PasswordEncoder passwordEncoder,
    SecurityProperties props
  ) {
    this.passwordEncoder = passwordEncoder;
    this.props = props;
  }
  
  public String generateToken(String email, List<String> roles) {
    Algorithm algo = Algorithm.HMAC256(props.knownSecret());
    return JWT.create()
      .withIssuer(props.knownIssuer())
      .withSubject(email)
      .withClaim("roles", roles)
      .withIssuedAt(new Date())
      .withExpiresAt(new Date(
        System.currentTimeMillis() + 3600_000
      ))
      .sign(algo)
    ;
  }

  public Optional<LoginResponse> loginUser(LoginRequest request) {

    List<String> roles = List.of("USER");
    boolean isMatched = (
      passwordEncoder
        .matches(request.password(), props.knownPasswordHash())
      && 
      request.email()
        .equals(props.knownEmail())
    );

    LoginResponse response = isMatched
      ? new LoginResponse(
        request.email(),
        generateToken(request.email(), roles),
        roles
      )
      : null
    ;
    return Optional.ofNullable(response);
  }
}
