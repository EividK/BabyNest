package com.assignment_two_starter.repository;

import com.assignment_two_starter.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
    // Custom method to delete a shopping cart for a given customer
    void deleteByCustomerUserId(Integer userId);
}

