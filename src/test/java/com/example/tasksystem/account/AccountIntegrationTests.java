package com.example.tasksystem.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountIntegrationTests {

    private static final String AUTH_EMAIL = "address@domain.net";
    private static final String AUTH_EMAIL_UPPER = "ADDRESS@DOMAIN.NET";
    private static final String AUTH_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void registerSameEmailTwice_returnsConflict() throws Exception {
        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setEmail(AUTH_EMAIL);
        firstRequest.setPassword(AUTH_PASSWORD);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        RegisterRequest secondRequest = new RegisterRequest();
        secondRequest.setEmail(AUTH_EMAIL_UPPER);
        secondRequest.setPassword(AUTH_PASSWORD);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict());
    }

}
