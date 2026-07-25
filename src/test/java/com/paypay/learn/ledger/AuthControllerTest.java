package com.paypay.learn.ledger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private IdempotencyRepository idempotencyRepository;

  @MockitoBean
  private AuthService authService;

  @BeforeEach
  void setUp() {
    when(
      authService.loginUser(any(LoginRequest.class))
    ).thenReturn(
      Optional.of(
        new LoginResponse(
          "a@b",
          "test_password",
          List.of("USER")
        )
      )
    );
  }

  @Test
  void validLoginShouldReturnOK() throws Exception {

    String testEmail = "a@b";
    String testPassword = "123";

    LoginRequest request = new LoginRequest(testEmail, testPassword);

    String jsonStr = objectMapper.writeValueAsString(request);

    MvcResult result = mockMvc.perform(
      post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonStr)
    )
      .andExpect(status().isOk())
      .andReturn()
    ;

    String responseBody = result.getResponse().getContentAsString();
    JsonNode response = objectMapper.readTree(responseBody);
    assertThat(response.get("email").asString())
      .isEqualTo(testEmail)
    ;
    assertThat(response.get("roles").asArray())
      .isNotNull()
      .isNotEmpty()
    ;
    assertThat(response.get("token").asString())
      .isNotNull()
      .isNotEmpty()
    ;
  }

}
