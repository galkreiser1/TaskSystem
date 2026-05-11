package com.example.tasksystem.security;

import com.example.tasksystem.account.Account;
import com.example.tasksystem.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    private static final String AUTH_EMAIL = "address@domain.net";
    private static final String AUTH_PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        Account account = new Account();
        account.setEmail(AUTH_EMAIL);
        account.setPassword(AUTH_PASSWORD);
        accountRepository.save(account);
    }

    @Test
    void getTasks_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasks_withAuthentication_returnsOk() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk());

    }

}
