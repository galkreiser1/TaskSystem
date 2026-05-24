package com.example.tasksystem.security;

import com.example.tasksystem.account.Account;
import com.example.tasksystem.account.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthTokenService {

    private static final long TOKEN_TTL_MILLIS = 24 * 60 * 60 * 1000;

    private final AuthTokenRepository authTokenRepository;
    private final AccountRepository accountRepository;

    public AuthTokenService(AuthTokenRepository authTokenRepository,
                            AccountRepository accountRepository) {
        this.authTokenRepository = authTokenRepository;
        this.accountRepository = accountRepository;

    }

        public TokenResponse generateToken(String email) {
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

            String tokenValue = "token-for-" + email + "-" + System.currentTimeMillis();
            AuthToken authToken = new AuthToken();
            authToken.setToken(tokenValue);
            authToken.setAccount(account);
            authToken.setExpiresAt(System.currentTimeMillis() + TOKEN_TTL_MILLIS);

            TokenResponse response = new TokenResponse();
            response.setToken(authTokenRepository.save(authToken).getToken());
            return response;
        }

        public AuthToken findValidToken(String tokenValue) {
            AuthToken authToken = authTokenRepository.findByToken(tokenValue)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

            if (authToken.getExpiresAt() < System.currentTimeMillis()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
            }

            return authToken;
        }


    }
