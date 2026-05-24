package com.example.tasksystem.account;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT);
        String password = request.getPassword();


        if (accountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(password);
        accountRepository.save(account);

    }
}