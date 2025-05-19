package com.assignment_two_starter.controller;

import com.assignment_two_starter.config.JwtUtil;
import com.assignment_two_starter.dto.WishlistItemDTO;
import com.assignment_two_starter.exceptions.ResourceNotFoundException;
import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.repository.CustomerRepository;
import com.assignment_two_starter.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/wishlist")
public class PublicWishlistController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private WishlistService wishlistService;

    /**
     * Public endpoint to view a users wishlist using a token
     */
    @GetMapping
    public ResponseEntity<?> getPublicWishlist(@RequestParam("token") String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            if (jwtUtil.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is expired");
            }
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
            List<WishlistItemDTO> wishlist = wishlistService.getWishlistForCustomer(customer);

            // Styled HTML page for wishlist response
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><title>Your Wishlist</title><style>")
                    .append("body { font-family: Arial, sans-serif; margin: 20px; }")
                    .append("table { width: 100%; border-collapse: collapse; }")
                    .append("th, td { border: 1px solid #ddd; padding: 8px; }")
                    .append("th { background-color: #f2f2f2; }")
                    .append("tr:nth-child(even) { background-color: #f9f9f9; }")
                    .append("tr:hover { background-color: #ddd; }")
                    .append("</style></head><body>");
            htmlBuilder.append("<h2>Your Wishlist</h2>");
            htmlBuilder.append("<table>");
            htmlBuilder.append("<tr><th>Product Name</th><th>Price</th><th>Note</th></tr>");
            for (WishlistItemDTO item : wishlist) {
                htmlBuilder.append("<tr>")
                        .append("<td>").append(item.getProductName()).append("</td>")
                        .append("<td>").append(item.getProductPrice()).append("</td>")
                        .append("<td>").append(item.getNote()).append("</td>")
                        .append("</tr>");
            }
            htmlBuilder.append("</table>");
            htmlBuilder.append("</body></html>");

            return ResponseEntity.ok().body(htmlBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }
}

