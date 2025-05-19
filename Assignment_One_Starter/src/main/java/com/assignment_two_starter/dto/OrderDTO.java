package com.assignment_two_starter.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderDTO {
    private Integer orderId;
    private Date orderDate;
    private BigDecimal totalAmount;
    private String status;
    private String customerName;
    private String shippingAddress;
    private String paymentStatus;
    private String paymentMethod;
    private List<OrderItemDTO> orderItems;

    public OrderDTO(Integer orderId, Date orderDate, BigDecimal totalAmount, String status,
                    String firstName, String lastName, String shippingAddress, String paymentStatus,
                    List<OrderItemDTO> orderItems, String paymentMethod) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.customerName = firstName + " " + lastName;
        this.shippingAddress = shippingAddress;
        this.paymentStatus = paymentStatus;
        this.orderItems = orderItems;
        this.paymentMethod = paymentMethod;
    }

    public Integer getOrderId() {
        return orderId;
    }
    public Date getOrderDate() {
        return orderDate;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public String getStatus() {
        return status;
    }
    public String getCustomerName() {
        return customerName;
    }
    public String getShippingAddress() {
        return shippingAddress;
    }
    public String getPaymentStatus() {
        return paymentStatus;
    }
    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
}



