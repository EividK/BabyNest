package com.assignment_two_starter.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderRequestDTO {
    private List<OrderItemRequestDTO> orderItems;
    private String paymentMethod;
}

