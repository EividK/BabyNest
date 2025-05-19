package com.assignment_two_starter.service;

import com.assignment_two_starter.dto.OrderItemDTO;
import com.assignment_two_starter.dto.OrderItemRequestDTO;
import com.assignment_two_starter.dto.OrderRequestDTO;
import com.assignment_two_starter.dto.OrderResponseDTO;
import com.assignment_two_starter.model.*;
import com.assignment_two_starter.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

@Service
public class OrderManagementService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderConverter orderConverter;

    /**
     * Retrieves the currently authenticated customer using the SecurityContext.
     * Assumes that the authenticated user's username is the customer's email.
     *
     * @return the authenticated Customer
     */
    private Customer getAuthenticatedCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("No authenticated user found");
        }
        String email = auth.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
    }

    /**
     * Places a new order using the details provided in the OrderRequestDTO.
     * This method updates the orders, order_items, payments tables and clears
     * the customer's shopping cart in one transaction.
     *
     * @param orderRequest the order details sent by the client
     * @return the saved Orders object
     */
    @Transactional
    public Orders placeOrder(OrderRequestDTO orderRequest) {
        // 1. Retrieve the currently authenticated customer.
        Customer customer = getAuthenticatedCustomer();

        // 2. Retrieve the shipping address.
        // For this example, we assume the customer's default shipping address is the first address.
        if (customer.getAddressesList() == null || customer.getAddressesList().isEmpty()) {
            throw new RuntimeException("No shipping address found for customer");
        }
        Address shippingAddress = customer.getAddressesList().get(0);

        // 3. Calculate the total amount by looking up product prices by name.
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequestDTO itemDTO : orderRequest.getOrderItems()) {
            // Lookup product by name.
            Product product = productRepository.findByName(itemDTO.getProductName())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductName()));
            // Convert product price (Double) to BigDecimal, then multiply by quantity.
            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = productPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 4. Create a new order.
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setShippingAddressId(shippingAddress);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setOrderDate(new Date());
        order.setAddressChangeFee(0.0);
        // Calculate an estimated shipping date (for example, current date + 3 days).
        Date estimatedShippingDate = Date.from(Instant.now().plus(3, ChronoUnit.DAYS));
        order.setEstimatedShippingDate(estimatedShippingDate);
        order.setOrderItemsList(new HashSet<>());
        order.setPaymentsList(new HashSet<>());

        Orders savedOrder = ordersRepository.save(order);

        // 5. Create order items.
        for (OrderItemRequestDTO itemDTO : orderRequest.getOrderItems()) {
            Product product = productRepository.findByName(itemDTO.getProductName())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductName()));
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            orderItem.setUnitPrice(productPrice);
            orderItem.setTotalPrice(productPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())).doubleValue());

            OrderItems savedOrderItem = orderItemsRepository.save(orderItem);
            savedOrder.getOrderItemsList().add(savedOrderItem);
        }

        // 6. Record payment information.
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(orderRequest.getPaymentMethod());
        payment.setStatus("PENDING");
        Payment savedPayment = paymentRepository.save(payment);
        savedOrder.getPaymentsList().add(savedPayment);

        // 7. Clear the customer's shopping cart.
        shoppingCartRepository.deleteByCustomerUserId(customer.getUserId());

        return savedOrder;
    }

    /**
     * Edits an existing order. This method:
     *  - Loads the order with its associations.
     *  - Checks that the authenticated customer is the owner.
     *  - Removes existing order items and payments.
     *  - Adds new order items based on the request and recalculates the total.
     *  - Creates a new payment record.
     *  - Updates shipping address and estimated shipping date.
     *  - Saves and reloads the order.
     *
     * @param orderId the ID of the order to edit
     * @param orderRequest the new order details
     * @return the updated Orders object (with associations initialized)
     */
    @Transactional
    public Orders editOrder(Integer orderId, OrderRequestDTO orderRequest) {
        // 1. Reload the order with associations.
        Orders order = ordersRepository.findByIdWithAssociations(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Ensure the authenticated customer owns this order.
        Customer authenticatedCustomer = getAuthenticatedCustomer();
        if (!order.getCustomer().getUserId().equals(authenticatedCustomer.getUserId())) {
            throw new RuntimeException("Unauthorized to edit this order");
        }

        // 3. Set the shipping address.
        if (authenticatedCustomer.getAddressesList() == null || authenticatedCustomer.getAddressesList().isEmpty()) {
            throw new RuntimeException("No shipping address found for customer");
        }
        Address shippingAddress = authenticatedCustomer.getAddressesList().get(0);
        order.setShippingAddressId(shippingAddress);

        // 4. Remove existing order items safely
        if (order.getOrderItemsList() != null) {
            orderItemsRepository.deleteAll(order.getOrderItemsList());
            order.getOrderItemsList().clear();
        }

        // 5. Recalculate total amount and add new order items.
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequestDTO itemDTO : orderRequest.getOrderItems()) {
            if (itemDTO.getProductName() == null) {
                throw new RuntimeException("Product name cannot be null in order items");
            }

            Product product = productRepository.findByName(itemDTO.getProductName())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductName()));

            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = productPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(productPrice);
            orderItem.setTotalPrice(itemTotal.doubleValue());

            order.getOrderItemsList().add(orderItemsRepository.save(orderItem));
        }
        order.setTotalAmount(totalAmount);

        // 6. Keep existing payment method
        if (order.getPaymentsList() == null || order.getPaymentsList().isEmpty()) {
            throw new RuntimeException("No payment record found for this order.");
        }
        Payment existingPayment = order.getPaymentsList().iterator().next();

        // 7. Remove old payment and create a new one with the same method
        paymentRepository.deleteAll(order.getPaymentsList());
        order.getPaymentsList().clear();

        Payment newPayment = new Payment();
        newPayment.setOrder(order);
        newPayment.setPaymentMethod(existingPayment.getPaymentMethod());
        newPayment.setStatus("PENDING");
        order.getPaymentsList().add(paymentRepository.save(newPayment));

        // 8. Update order date and estimated shipping date.
        order.setOrderDate(new Date());
        order.setEstimatedShippingDate(Date.from(Instant.now().plus(3, ChronoUnit.DAYS)));

        return ordersRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderResponseDTO(Integer orderId) {
        Orders order = ordersRepository.findByIdWithAssociations(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderConverter.convertToDto(order);
    }
}


