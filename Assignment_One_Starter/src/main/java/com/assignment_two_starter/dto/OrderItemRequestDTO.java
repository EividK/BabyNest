package com.assignment_two_starter.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequestDTO {
    private String productName;
    private int quantity;
}

