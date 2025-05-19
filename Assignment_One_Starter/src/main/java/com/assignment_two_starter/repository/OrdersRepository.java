package com.assignment_two_starter.repository;

import com.assignment_two_starter.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    @Query("SELECT o FROM Orders o " +
            "LEFT JOIN FETCH o.orderItemsList " +
            "LEFT JOIN FETCH o.paymentsList " +
            "WHERE o.orderId = :orderId")
    Optional<Orders> findByIdWithAssociations(@Param("orderId") Integer orderId);

}

