package com.assignment_two_starter.repository;

import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByCustomer(Customer customer);
}