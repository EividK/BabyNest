package com.assignment_two_starter.service;

import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.model.RefreshToken;
import com.assignment_two_starter.repository.CustomerRepository;
import com.assignment_two_starter.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private static final long REFRESH_TOKEN_DURATION = 7 * 24 * 60 * 60; //7 days in seconds

    public String createRefreshToken(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(REFRESH_TOKEN_DURATION);

        RefreshToken refreshToken = new RefreshToken(token, expiryDate, customer);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public boolean isValidRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                //ensure token is still valid
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .isPresent();
    }

    public String getUsernameFromToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> rt.getCustomer().getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}

