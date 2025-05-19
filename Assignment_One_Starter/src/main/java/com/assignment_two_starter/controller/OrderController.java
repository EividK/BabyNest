package com.assignment_two_starter.controller;

import com.assignment_two_starter.dto.OrderDTO;
import com.assignment_two_starter.dto.OrderRequestDTO;
import com.assignment_two_starter.dto.OrderResponseDTO;
import com.assignment_two_starter.model.Orders;
import com.assignment_two_starter.service.InvoiceService;
import com.assignment_two_starter.service.OrderConverter;
import com.assignment_two_starter.service.OrderManagementService;
import com.assignment_two_starter.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;
    private OrderManagementService orderManagementService;
    private final OrderConverter orderConverter;

    @Autowired
    public OrderController(OrderService orderService, InvoiceService invoiceService, OrderManagementService orderManagementService, OrderConverter orderConverter) {
        this.orderService = orderService;
        this.invoiceService = invoiceService;
        this.orderManagementService = orderManagementService;
        this.orderConverter = orderConverter;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{orderId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, "application/x-yaml", "text/tab-separated-values"})
    public ResponseEntity<?> getOrder(@PathVariable Integer orderId,
                                      @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        try {
            OrderDTO orderDTO = orderService.getOrderById(orderId);
            // Return in requested format based on Accept header.
            switch (acceptHeader) {
                case MediaType.APPLICATION_XML_VALUE:
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(orderDTO);
                case "application/x-yaml":
                    return ResponseEntity.ok().contentType(MediaType.valueOf("application/x-yaml")).body(orderDTO);
                case "text/tab-separated-values":
                    return ResponseEntity.ok().contentType(MediaType.valueOf("text/tab-separated-values"))
                            .body(orderToTSV(orderDTO));
                default:
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(orderDTO);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, "application/x-yaml", "text/tab-separated-values"})
    public ResponseEntity<?> getAllOrders(
            @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader,
            @RequestParam(defaultValue = "0") int page, // Default page 0
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> ordersPage = orderService.getAllOrders(pageable);

        // If there are no orders at all, return a message to the user.
        if (ordersPage.getTotalElements() == 0) {
            Map<String, Object> noContentResponse = new HashMap<>();
            noContentResponse.put("message", "No orders found. Please place an order to see it listed here.");
            return ResponseEntity.ok(noContentResponse);
        }

        // Check if the requested page is out of range.
        if (page >= ordersPage.getTotalPages()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Requested page " + page + " is out of range. Total pages: " + ordersPage.getTotalPages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Build a response map with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("orders", ordersPage.getContent());
        response.put("Page", ordersPage.getNumber());
        response.put("Total Pages", ordersPage.getTotalPages());
        response.put("Total Orders", ordersPage.getTotalElements());
        response.put("Orders per page", ordersPage.getSize());

        // Return response in requested format.
        switch (acceptHeader) {
            case MediaType.APPLICATION_XML_VALUE:
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(response);
            case "application/x-yaml":
                return ResponseEntity.ok().contentType(MediaType.valueOf("application/x-yaml")).body(response);
            case "text/tab-separated-values":
                String tsvOutput = ordersPage.getContent().stream()
                        .map(this::orderToTSV)
                        .collect(Collectors.joining("\n"));
                return ResponseEntity.ok().contentType(MediaType.valueOf("text/tab-separated-values")).body(tsvOutput);
            default:
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        }
    }

    // PDF invoice using OpenPDF
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{orderId:\\d+}/invoice", produces = MediaType.APPLICATION_PDF_VALUE) // To match only numeric IDs
    public ResponseEntity<?> getInvoice(@PathVariable Integer orderId) {
        try {
            OrderDTO orderDTO = orderService.getOrderById(orderId);
            if (orderDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"Order not found\", \"orderId\": " + orderId + "}");
            }

            byte[] pdfBytes = invoiceService.generateInvoice(orderDTO);
            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"Failed to generate invoice for order\", \"orderId\": " + orderId + "}");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("invoice_" + orderId + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"Order not found or cannot be processed\", \"orderId\": " + orderId + "}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Unexpected error while generating invoice\", \"orderId\": " + orderId + "}");
        }
    }

    // Helper method to convert OrderDTO to TSV format including extra fields.
    private String orderToTSV(OrderDTO order) {
        return String.join("\t",
                order.getOrderId().toString(),
                order.getOrderDate().toString(),
                order.getTotalAmount().toString(),
                order.getStatus(),
                order.getCustomerName(),
                order.getShippingAddress(),
                order.getPaymentStatus()
        );
    }

    // Post endpoint for placing an order
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO orderRequest) {
        try {
            Orders order = orderManagementService.placeOrder(orderRequest);
            OrderResponseDTO orderDTO = orderManagementService.getOrderResponseDTO(order.getOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
        } catch (Exception e) {
            // Debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to place order: " + e.getMessage());
        }
    }

    // PUT endpoint for editing an order
    @PutMapping("/edit/{orderId}")
    public ResponseEntity<?> editOrder(@PathVariable Integer orderId, @RequestBody OrderRequestDTO orderRequest) {
        try {
            Orders order = orderManagementService.editOrder(orderId, orderRequest);
            OrderResponseDTO dto = orderManagementService.getOrderResponseDTO(order.getOrderId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            // Debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to edit order: " + e.getMessage());
        }
    }
}

