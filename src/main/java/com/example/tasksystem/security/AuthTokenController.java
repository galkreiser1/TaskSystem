package com.example.tasksystem.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/token")
public class AuthTokenController {

    private final AuthTokenService authTokenService;

    public AuthTokenController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @PostMapping()
    public ResponseEntity<TokenResponse> createToken(Authentication authentication) {
        String email = authentication.getName();
        TokenResponse response = authTokenService.generateToken(email);
        return ResponseEntity.ok(response);
    }
}
