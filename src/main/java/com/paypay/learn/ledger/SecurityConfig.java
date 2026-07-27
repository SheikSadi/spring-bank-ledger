/*
As soon as spring-boot-starter-security is added to build.gradle.kts, Spring Security automatically activates a default security filter chain that locks down 100% of HTTP endpoints in the application.

Until you create SecurityConfig.java with your custom SecurityFilterChain bean, Spring Security has no idea that /auth/login should be publicly accessible.
*/

package com.paypay.learn.ledger;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final SecurityProperties props;

  public SecurityConfig(SecurityProperties props) {
    this.props = props;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity httpSecurity
  ) throws Exception {

    httpSecurity
      // Disable csrf for stateless REST APIs
      .csrf(csrf -> csrf.disable())
      // Set session creation to stateless
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      // Declare public vs protected endpoints
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
        .requestMatchers("/accounts/**").hasAuthority("SCOPE_USER")
        .requestMatchers("/users/**").hasAuthority("SCOPE_ADMIN")
        .requestMatchers(
          HttpMethod.GET,
          "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
        ).permitAll()
        .anyRequest().authenticated()
      )
      // Attach native oauth2 JWT Bearer token validation
      .oauth2ResourceServer(
        oauth2 -> oauth2.jwt(Customizer.withDefaults())
      )
    ;

    // Build and returned the assembled filter chain
    return httpSecurity.build();
  }

  /*
  Incoming Request ("Authorization: Bearer <token>")
          │
          ▼
  BearerTokenAuthenticationFilter
          │
          │ Injects your @Bean JwtDecoder
          ▼ 
  jwtDecoder.decode(token) ➔ Verifies signature with your secret key!
  */
  @Bean
  public JwtDecoder jwtDecoder() {
    SecretKey secretKey = new SecretKeySpec(
      props.knownSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"
    );
    return NimbusJwtDecoder.withSecretKey(secretKey).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
