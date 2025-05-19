package com.assignment_two_starter.repository;;

import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.model.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<WishlistItem, Integer> {
    List<WishlistItem> findByCustomer(Customer customer);

    Page<WishlistItem> findByCustomer(Customer customer, Pageable pageable);
}

