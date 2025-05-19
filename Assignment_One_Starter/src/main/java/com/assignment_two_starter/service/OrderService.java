package com.assignment_two_starter.service;

import com.assignment_two_starter.dto.OrderDTO;
import com.assignment_two_starter.dto.OrderItemDTO;
import com.assignment_two_starter.dto.OrderResponseDTO;
import com.assignment_two_starter.model.Orders;
import com.assignment_two_starter.model.Payment;
import com.assignment_two_starter.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrdersRepository orderRepository;

    @Autowired
    private OrderConverter orderConverter;

    // Returns paginated OrderResponseDTO objects using the converter
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderConverter::convertToOrderDto);
    }

    public OrderDTO getOrderById(Integer orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderConverter.convertToOrderDto(order);
    }
}
