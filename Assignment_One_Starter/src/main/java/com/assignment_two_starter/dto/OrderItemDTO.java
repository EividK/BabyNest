package com.assignment_two_starter.dto;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Integer orderItemId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private Double totalPrice;

    public OrderItemDTO() { }

    public OrderItemDTO(Integer orderItemId, String productName, int quantity, BigDecimal unitPrice, Double totalPrice) {
        this.orderItemId = orderItemId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }
    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    public Double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}


