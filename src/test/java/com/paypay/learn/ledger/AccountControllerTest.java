package com.paypay.learn.ledger;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService service;

    @MockitoBean
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String accountId = "test-1234";
    private final String missingId = "missing-id";

    @BeforeEach
    void setUp() {
        Account defaultAccount = new Account(accountId, "Sadi", "JPY", BigDecimal.valueOf(1000));
        when(service.getAccountById(accountId)).thenReturn(Optional.of(defaultAccount));
        when(service.getAccountById(missingId)).thenReturn(Optional.empty());
    }

    @Test
    void shouldReturn400BadRequest() throws Exception {
        // Invalid currency format
        String jsonStr = """
            {
                "currency": "ABCD"
            }
            """
        ;
        mockMvc.perform(
            put("/accounts/id").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(jsonStr)
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("currency"))
        ;
    }

    @Test
    void shouldReturn404NotFound() throws Exception {
        when(
            service.updateAccount(anyString(), any(UpdateAccountRequest.class))
        ).thenThrow(new AccountNotFoundException("{id}"));

        // Invalid currency format
        String jsonStr = """
            {
                "currency": "ABC"
            }
            """
            ;
        mockMvc.perform(
                put("/accounts/id").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(jsonStr)
            )
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.message").value(containsString("Account not found")))
            .andExpect(jsonPath("$.fieldErrors").value(hasSize(0)))
        ;
    }


    @Test
    void shouldReturnAccountWhenFound() throws Exception {
        mockMvc.perform(
            get("/accounts/" + accountId).with(jwt())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(accountId))
        ;
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(
                get("/accounts/" + missingId).with(jwt())
            )
            .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldReturn400WhenInvalidRequest() throws Exception {
        CreateAccountRequest invalidRequest = new CreateAccountRequest("", "INVALID", BigDecimal.valueOf(-100));
        
        String jsonStr = objectMapper.writeValueAsString(invalidRequest);
        
        
        mockMvc.perform(
                post("/accounts")
                .contentType("application/json")
                .content(jsonStr)
            )
            .andExpect(status().is4xxClientError())
        ;
    }

    @Test
    void shouldReturn422WhenLowBalance() throws Exception {
        when(
            service.debitAccount(anyString(), any(BigDecimal.class))
        )
            .thenThrow(
                new InsufficientFundsException(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(2000))
            )
        ;

        String jsonStr = """
            {
                "amount": 2000
            }
            """;

        mockMvc.perform(
            post("/accounts/" + accountId + "/debit")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
        )
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.message").value(containsString("exceeds the current balance")))
        ;
    }

    @Test
    void shouldReturn500WhenUnexpected() throws Exception {
        when(
            service.getAccountById(anyString())
        )
            .thenThrow(
                new RuntimeException("Mocked: database connection timed out")
            )
        ;

        mockMvc.perform(
            get("/accounts/" + accountId).with(jwt())
        )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value(containsString("unexpected error occured")))
        ;
    }

    @Test
    void responseHeaderShouldContainCorrelationId() throws Exception {
        mockMvc.perform(
            get("/accounts/" + accountId).with(jwt())
        )
            .andExpect(header().exists("X-Correlation-Id"))
        ;

        String correlationId = "test-correlation-id";

        mockMvc.perform(
            get("/accounts/" + accountId)
                .with(jwt())
                .header("X-Correlation-Id", correlationId)
        )
            .andExpect(header().string("X-Correlation-Id", correlationId))
        ;
    }
}
