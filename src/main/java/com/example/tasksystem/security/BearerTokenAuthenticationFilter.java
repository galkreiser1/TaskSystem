package com.example.tasksystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;
    private final AccountUserDetailsService accountUserDetailsService;

    public BearerTokenAuthenticationFilter(AuthTokenService authTokenService, AccountUserDetailsService accountUserDetailsService) {
        this.authTokenService = authTokenService;
        this.accountUserDetailsService = accountUserDetailsService;

    }
        @Override
        protected void doFilterInternal (
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
      ) throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String sentToken = authHeader.substring(7);
                    AuthToken existingToken = authTokenService.findValidToken(sentToken);
                    String email = existingToken.getAccount().getEmail();
                    UserDetails userDetails = accountUserDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                catch (RuntimeException e) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }

            filterChain.doFilter(request, response);
        }
    }

