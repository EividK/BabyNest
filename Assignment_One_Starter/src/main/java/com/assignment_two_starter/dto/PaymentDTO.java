package com.assignment_two_starter.dto;

import lombok.Data;

@Data
public class PaymentDTO {
    private Integer paymentId;
    private String paymentMethod;
    private String status;
}
