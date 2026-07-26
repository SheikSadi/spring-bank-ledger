package com.paypay.learn.ledger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private static Stream<LoginRequest> provideLoginRequests() {
    return Stream.of(
      new LoginRequest("admin@example.com", "admin_password"),
      new LoginRequest("test@example.com", "test_password")
    );
  }

  @ParameterizedTest
  @MethodSource("provideLoginRequests")
  void testLoginWithValidCredentials(LoginRequest request) throws Exception {

    String body = objectMapper.writeValueAsString(request);

    MvcResult result = mockMvc.perform(
      post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").isNotEmpty())
      .andExpect(jsonPath("$.roles").isArray())
      .andReturn()
    ;

    String responseBody = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    String token = jsonNode.get("token").asString();

    assertThat(token).isNotBlank();

    mockMvc.perform(
      get("/accounts")
        .header("Authorization", "Bearer " + token)
    )
      .andExpect(status().isOk())
    ;
  }

  @ParameterizedTest
  @CsvSource({
    "admin@example.com, admin_password, true",
    "test@example.com, test_password, false"
  })
  void testHasAdminAuthority(
    String email, String password, boolean isOk
  ) throws Exception {
    LoginRequest request = new LoginRequest(email, password);
    String body = objectMapper.writeValueAsString(request);
    
    MvcResult result = mockMvc.perform(
      post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").isNotEmpty())
      .andExpect(jsonPath("$.roles").isArray())
      .andReturn()
    ;
    
    String responseBody = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    String token = jsonNode.get("token").asString();

    assertThat(token).isNotBlank();

    ResultMatcher expectedStatus = isOk
      ? status().isOk()
      : status().isForbidden()
    ;

    mockMvc.perform(
      get("/admin/users")
        .header("Authorization", "Bearer " + token)
    )
      .andExpect(expectedStatus)
    ;
  }

  @Test
  void testLoginWithInvalidCredentials() throws Exception {

    mockMvc.perform(
      post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "email": "admin@example.com",
            "password": "fake_doesn't_exist"
          }
        """)
    )
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.message").value("Invalid Login: The credentials don't match our record."))
    ;
  }

}
