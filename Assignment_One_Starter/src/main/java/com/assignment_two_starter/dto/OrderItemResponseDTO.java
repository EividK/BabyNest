package com.assignment_two_starter.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Integer orderItemId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private Double totalPrice;
}

