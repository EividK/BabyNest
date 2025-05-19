package com.assignment_two_starter.repository;

import com.assignment_two_starter.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
