package com.example.tasksystem.security;

import com.example.tasksystem.account.Account;
import com.example.tasksystem.account.AccountRepository;
import com.example.tasksystem.task.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TokenAuthIntegrationTests {

    private static final String AUTH_EMAIL = "address@domain.net";
    private static final String AUTH_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @BeforeEach
    void setUp() {
        authTokenRepository.deleteAll();
        taskRepository.deleteAll();
        accountRepository.deleteAll();

        Account account = new Account();
        account.setEmail(AUTH_EMAIL);
        account.setPassword(AUTH_PASSWORD);
        accountRepository.save(account);
    }

    @Test
    void createToken_withValidBasicAuth_returnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void createToken_withInvalidBasicAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(AUTH_EMAIL, "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasks_withValidBearerToken_returnsOk() throws Exception {
        MvcResult tokenResult = mockMvc.perform(post("/api/auth/token")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse tokenResponse = objectMapper.readValue(
                tokenResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + tokenResponse.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    void getTasks_withInvalidBearerToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

    }
}