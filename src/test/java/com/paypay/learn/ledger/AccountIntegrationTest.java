package com.paypay.learn.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
// import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@SpringBootTest
@AutoConfigureMockMvc
public class AccountIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private LedgerProperties properties;

    @Autowired
    private AuthService authService;

    private final String accountOwner = "Sadi";

    protected final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    protected String AUTH_BEARER;

    protected final String AUTH_HEADER = "Authorization";

    @BeforeEach
    void setUp() {
        String email = "a@b";
        // LoginRequest request = new LoginRequest(email, "123");
        this.AUTH_BEARER = "Bearer " + authService.generateToken(email, List.of("USER"));
    }

    private String createAccountHelper(
        String owner, String currency, BigDecimal balance
    ) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(owner, currency, balance);
        String jsonStr = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
            post("/accounts")
                .header(AUTH_HEADER, AUTH_BEARER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn()
        ;
        String responseBody = result.getResponse().getContentAsString();
        JsonNode tree = objectMapper.readTree(responseBody);
        return tree.get("id").asString();
    }

    @Test
    void shouldCreateAccount() throws Exception {
        String currency = "JPY";
        BigDecimal balance = BigDecimal.valueOf(1000);
        CreateAccountRequest request = new CreateAccountRequest(
            accountOwner, currency, balance
        );
        String jsonStr = objectMapper.writeValueAsString(request);

        mockMvc.perform(
            post("/accounts")
                .header(AUTH_HEADER, AUTH_BEARER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
        )
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.owner").value(accountOwner))
            .andExpect(jsonPath("$.currency").value(currency))
            .andExpect(jsonPath("$.balance").value(balance.intValue()))
            .andReturn();
    }

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
            .andExpect(jsonPath("$.currency").value(properties.defaultCurrency()))
        ;
    }

    @Test
    void shouldRetrieveAccount() throws Exception {
        String currency = "USD";
        BigDecimal balance = BigDecimal.valueOf(2000);
        String accountId = createAccountHelper(accountOwner, currency, balance);

        mockMvc.perform(
            get("/accounts/" + accountId)
                .header(AUTH_HEADER, AUTH_BEARER)   
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.owner").value(accountOwner))
            .andExpect(jsonPath("$.currency").value(currency))
            .andExpect(jsonPath("$.balance").value(balance.intValue()))
        ;
    }

    @Test
    void shouldUpdateAccount() throws Exception {
        String currency = "USD";
        BigDecimal balance = BigDecimal.valueOf(2000);
        String accountId = createAccountHelper(accountOwner, currency, balance);

        String newCurrency = "USD";
        BigDecimal newBalance = BigDecimal.valueOf(9982);
        UpdateAccountRequest updateRequest = new UpdateAccountRequest(newCurrency, newBalance);
        String jsonStr = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(
            put("/accounts/" + accountId)
                .header(AUTH_HEADER, AUTH_BEARER)                
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(accountId))
            .andExpect(jsonPath("$.owner").value(accountOwner))
            .andExpect(jsonPath("$.currency").value(newCurrency))
            .andExpect(jsonPath("$.balance").value(newBalance.intValue()))
        ;
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        String currency = "GBP";
        BigDecimal balance = BigDecimal.valueOf(321);
        String accountId = createAccountHelper(accountOwner, currency, balance);

        mockMvc.perform(
            delete("/accounts/" + accountId)
                .header(AUTH_HEADER, AUTH_BEARER)
        )
            .andExpect(status().isNoContent())
        ;

        mockMvc.perform(
            get("/accounts/" + accountId)
                .header(AUTH_HEADER, AUTH_BEARER)
        )
            .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldFailWhenDebitExceedsBalance() throws Exception {
        String currency = "GBP";
        BigDecimal balance = BigDecimal.valueOf(321);
        String accountId = createAccountHelper(accountOwner, currency, balance);


        mockMvc.perform(
            post("/accounts/" + accountId + "/debit")
                .header(AUTH_HEADER, AUTH_BEARER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"amount": 1000}     
                    """
                )
        )
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.message").value(containsString("exceeds the current balance")))
        ;
    }

    @Test
    void statusShouldBeUp() throws Exception {
        mockMvc.perform(
            get("/actuator/health")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
        ;
    }

    @Test
    void shouldRespectIdempotency() throws Exception {
        String idempotencyId = "test-idempotency-id-1";
        CreateAccountRequest request = new CreateAccountRequest(accountOwner, "TST", BigDecimal.valueOf(1000));
        String jsonStr = objectMapper.writeValueAsString(request);
        
        MvcResult result = mockMvc.perform(
            post("/accounts")
                .header(AUTH_HEADER, AUTH_BEARER)
                .header(IDEMPOTENCY_HEADER, idempotencyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())        
            .andReturn()
        ;

        String responseBody = result.getResponse().getContentAsString();
        JsonNode tree = objectMapper.readTree(responseBody);
        String createdId = tree.get("id").asString();

        // Duplicate request with same idempotency key
        mockMvc.perform(
            post("/accounts")
                .header(AUTH_HEADER, AUTH_BEARER)
                .header(IDEMPOTENCY_HEADER, idempotencyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(createdId))
        ;
    }

    @Nested
    // @ActiveProfiles("in-memory")
    class ConcurrencyTests {

        @Test
        void shouldRespectIdempotencyConcurrently() throws Exception {
            // Arrange
            String idempotencyId = "test-concurrent-idempotency-id-1";
            String uniqueOwner = "test-concurrent-user:" + UUID.randomUUID();
            CreateAccountRequest request = new CreateAccountRequest(uniqueOwner, "TST", BigDecimal.valueOf(1000));
            String jsonStr = objectMapper.writeValueAsString(request);
            
            Supplier<String> createAccount = () -> {
                try {
                    MvcResult result = mockMvc.perform(
                        post("/accounts")
                            .header(AUTH_HEADER, AUTH_BEARER)
                            .header(IDEMPOTENCY_HEADER, idempotencyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonStr)
                    )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andReturn();
                    ;
                    String responseBody = result.getResponse().getContentAsString();
                    JsonNode tree = objectMapper.readTree(responseBody);
                    String createdId = tree.get("id").asString();
                    return createdId;
                } catch (Exception ex){
                    throw new RuntimeException(ex);
                }
            };

            List<String> createdIds = TestUtils.runConcurrentlyWithSupplier(2, createAccount);

            assertThat(createdIds.get(0).equals((createdIds.get(1))));

        }
    }
}
