package com.paypay.learn.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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

  @Test
  void testLoginWithValidCredentials() throws Exception {

    MvcResult result = mockMvc.perform(
      post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "email": "biis.saadi@gmail.com",
            "password": "test_password"
          }
        """)
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("biis.saadi@gmail.com"))
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

  @Test
  void testLoginWithInvalidCredentials() throws Exception {

    mockMvc.perform(
      post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "email": "biis.saadi@gmail.com",
            "password": "fake_doesn't_exist"
          }
        """)
    )
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.message").value("Invalid Login: The credentials don't match our record."))
    ;
  }

}
