package com.assignment_two_starter.service;

import com.assignment_two_starter.dto.*;
import com.assignment_two_starter.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderConverter {

    public OrderResponseDTO convertToDto(Orders order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setEstimatedShippingDate(order.getEstimatedShippingDate());

        // Convert shipping address
        if(order.getShippingAddressId() != null) {
            ShippingAddressDTO addrDto = new ShippingAddressDTO();
            Address address = order.getShippingAddressId();
            addrDto.setAddressId(address.getAddressId());
            addrDto.setStreetAddress(address.getStreetAddress());
            addrDto.setCity(address.getCity());
            addrDto.setCounty(address.getCounty());
            addrDto.setPostalCode(address.getPostalCode());
            addrDto.setCountry(address.getCountry());
            dto.setShippingAddress(addrDto);
        }

        // Convert order items
        if(order.getOrderItemsList() != null) {
            List<OrderItemResponseDTO> items = order.getOrderItemsList().stream().map(item -> {
                OrderItemResponseDTO itemDto = new OrderItemResponseDTO();
                itemDto.setOrderItemId(item.getOrderItemId());
                // Assuming product has a getName() method:
                itemDto.setProductName(item.getProduct().getName());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setUnitPrice(item.getUnitPrice());
                itemDto.setTotalPrice(item.getTotalPrice());
                return itemDto;
            }).collect(Collectors.toList());
            dto.setOrderItems(items);
        }

        // Convert payment if available
        if(order.getPaymentsList() != null && !order.getPaymentsList().isEmpty()) {
            Payment payment = order.getPaymentsList().stream().findFirst().orElse(null);
            if (payment != null) {
                PaymentDTO paymentDto = new PaymentDTO();
                paymentDto.setPaymentId(payment.getPaymentId());
                paymentDto.setPaymentMethod(payment.getPaymentMethod());
                paymentDto.setStatus(payment.getStatus());
                dto.setPayment(paymentDto);
            }
        }

        return dto;
    }

    public OrderDTO convertToOrderDto(Orders order) {
        // Extract customer name from Order's customer details
        String firstName = order.getCustomer() != null ? order.getCustomer().getFirstName() : "";
        String lastName  = order.getCustomer() != null ? order.getCustomer().getLastName()  : "";

        // Build a shipping address string from the Address entity
        String shippingAddress = "No Address";
        if (order.getShippingAddressId() != null) {
            Address address = order.getShippingAddressId();
            shippingAddress = address.getStreetAddress() + ", "
                    + address.getCity() + ", "
                    + address.getPostalCode();
        }

        // Determine payment status using the first payment's status if available
        String paymentStatus = "Not Paid";
        String paymentMethod = "N/A";
        if (order.getPaymentsList() != null && !order.getPaymentsList().isEmpty()) {
            Payment payment = order.getPaymentsList().iterator().next();
            paymentStatus = payment.getStatus();
            paymentMethod = payment.getPaymentMethod();
        }

        // Convert order items to a List<OrderItemDTO>
        List<OrderItemDTO> orderItems = new ArrayList<>();
        if (order.getOrderItemsList() != null) {
            orderItems = order.getOrderItemsList().stream().map(item -> {
                return new OrderItemDTO(
                        item.getOrderItemId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                );
            }).collect(Collectors.toList());
        }

        return new OrderDTO(
                order.getOrderId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                firstName,
                lastName,
                shippingAddress,
                paymentStatus,
                orderItems,
                paymentMethod
        );
    }
}

