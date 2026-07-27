package com.paypay.learn.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;


@WebMvcTest(UserController.class)
public class UserControllerTest {


    @MockitoBean
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private static final String testEmail = System.getenv("TEST_EMAIL");
    private static final String testPassword = System.getenv("TEST_PASSWORD");

    @BeforeEach
    void setUp() {

        User user = User.from(new CreateUserRequest(
            null,
            testEmail,
            null,
            null,
            "USER",
            testPassword
        ));

        when(userService.createUser(any(CreateUserRequest.class)))
           .thenReturn(user)
        ;

        when(userService.getUsers()).thenReturn(List.of(user));
    }

    @Test
    void shouldreturnUser() throws Exception {

        mockMvc.perform(
            get("/users")
                .with(jwt())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").exists())
            .andExpect(jsonPath("$[0].role").value("USER"))
        ;

    }

    @Test
    void shouldCreateUser() throws Exception {
        String email = System.getenv("TEST_EMAIL");
        String password = System.getenv("TEST_PASSWORD");
        String role = "USER";

        String body = objectMapper.writeValueAsString(
            new CreateUserRequest(
                null,
                email,
                null,
                null,
                role,
                password
            )
        );

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt())
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value(role))
        ;

    }

}
