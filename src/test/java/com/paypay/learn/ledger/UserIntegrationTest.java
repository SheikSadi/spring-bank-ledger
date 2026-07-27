package com.paypay.learn.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String AUTH_HEADER = "Authorization";

    private static final String adminEmail = System.getenv("ADMIN_EMAIL");
    private static final String adminPassword = System.getenv("ADMIN_PASSWORD");
    private static final String testEmail = System.getenv("TEST_EMAIL");
    private static final String testPassword = System.getenv("TEST_PASSWORD");


    @Test
    void testUserCRUD() throws Exception {

        MvcResult result = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new LoginRequest(
                            adminEmail,
                            adminPassword
                        )
                    )
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andReturn()
        ;

        String responseBody = result.getResponse().getContentAsString();
        JsonNode tree = objectMapper.readTree(responseBody);
        String token = tree.get("token").asString();

        String AUTH_BEARER = "Bearer " + token;

        CreateUserRequest request = new CreateUserRequest(
            null,
            testEmail,
            null,
            null,
            "USER",
            testPassword
        );

        String body = objectMapper.writeValueAsString(request);

        // Create
        result = mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header(AUTH_HEADER, AUTH_BEARER)
        )
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andReturn()
        ;

        String location = result.getResponse().getHeader("Location");
        String id = location.substring(location.lastIndexOf("/") + 1);
        String endpoint = "/users/" + id;

        // Read
        mockMvc.perform(
            get(endpoint)
                .header(AUTH_HEADER, AUTH_BEARER)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
        ;

        // Update
        // TODO

        // Delete
        mockMvc.perform(
            delete(endpoint)
                .header(AUTH_HEADER, AUTH_BEARER)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
        ;

    }

}
