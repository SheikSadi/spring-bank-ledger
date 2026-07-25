package com.paypay.learn.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"dev", "in-memory"})
public class AccountDevIntegrationTest extends AccountIntegrationTest {
    private final String expectedCurrency = "GBP";

    @Test
    void shouldCreateAccountWithDefault() throws Exception {
        // Missing currency
        String jsonStr = """
            {
                "owner": "Sadi",
                "initialBalance": 10000
            }
            """;
        mockMvc.perform(
                post("/accounts")
                    .header(AUTH_HEADER, AUTH_BEARER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonStr)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.owner").value("Sadi"))
            .andExpect(jsonPath("$.balance").value(10000))
            .andExpect(jsonPath("$.currency").value(expectedCurrency))
        ;
    }
}
