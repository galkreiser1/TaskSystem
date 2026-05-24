package com.example.tasksystem.security;

import com.example.tasksystem.account.Account;
import jakarta.persistence.*;

@Entity
public class AuthToken {

    @Id
    @GeneratedValue
    private Long id;

    private String token;

    private Long expiresAt;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;


    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }



}
