package com.assignment_two_starter.repository;

import com.assignment_two_starter.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    //permits looking up a customer by their email address
    Optional<Customer> findByEmail(String email);

    @Query(value = "SELECT r.id, r.name FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId", nativeQuery = true)
    List<Object[]> findRolesByUserId(@Param("userId") Integer userId);
}
