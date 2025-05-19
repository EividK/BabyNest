package com.assignment_two_starter.controller;

import com.assignment_two_starter.config.JwtUtil;
import com.assignment_two_starter.dto.WishlistItemDTO;
import com.assignment_two_starter.dto.WishlistItemRequestDTO;
import com.assignment_two_starter.exceptions.ResourceNotFoundException;
import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.repository.CustomerRepository;
import com.assignment_two_starter.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // If pagination parameters are provided, returns a paginated response
    @GetMapping
    public ResponseEntity<?> getWishlist(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        Customer customer = null;

        // If token is provided, use it to identify the customer
        if (token != null) {
            try {
                String email = jwtUtil.extractUsername(token);
                if (jwtUtil.isTokenExpired(token)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is expired");
                }
                customer = customerRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
            }
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }
            String email = auth.getName();
            customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        }

        if (page != null && size != null) {
            Page<WishlistItemDTO> paginatedWishlist = wishlistService.getWishlistPage(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("wishlist", paginatedWishlist.getContent());
            response.put("page", paginatedWishlist.getNumber());
            response.put("totalPages", paginatedWishlist.getTotalPages());
            response.put("totalItems", paginatedWishlist.getTotalElements());
            response.put("pageSize", paginatedWishlist.getSize());
            return ResponseEntity.ok(response);
        } else {
            List<WishlistItemDTO> wishlist = wishlistService.getWishlist();
            return ResponseEntity.ok(wishlist);
        }
    }

    // Add a new item to the wishlist
    @PostMapping
    public ResponseEntity<?> addWishlistItem(@Valid @RequestBody WishlistItemRequestDTO requestDTO) {
        try {
            WishlistItemDTO dto = wishlistService.addItem(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to add wishlist item: " + e.getMessage());
        }
    }

    // Update an existing wishlist item
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateWishlistItem(@PathVariable Integer itemId, @Valid @RequestBody WishlistItemRequestDTO requestDTO) {
        try {
            WishlistItemDTO dto = wishlistService.updateItem(itemId, requestDTO);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update wishlist item: " + e.getMessage());
        }
    }

    // Remove a wishlist item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> removeWishlistItem(@PathVariable Integer itemId) {
        try {
            wishlistService.removeItem(itemId);
            return ResponseEntity.ok("Wishlist item removed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to remove wishlist item: " + e.getMessage());
        }
    }
}

