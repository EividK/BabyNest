package com.assignment_two_starter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Integer orderId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date orderDate;

    private BigDecimal totalAmount;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date estimatedShippingDate;

    private ShippingAddressDTO shippingAddress;
    private List<OrderItemResponseDTO> orderItems;
    private PaymentDTO payment;
}

