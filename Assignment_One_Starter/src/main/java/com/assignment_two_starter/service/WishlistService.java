package com.assignment_two_starter.service;

import com.assignment_two_starter.config.JwtUtil;
import com.assignment_two_starter.dto.WishlistItemDTO;
import com.assignment_two_starter.dto.WishlistItemRequestDTO;
import com.assignment_two_starter.exceptions.ResourceNotFoundException;
import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.model.Product;
import com.assignment_two_starter.model.WishlistItem;
import com.assignment_two_starter.repository.CustomerRepository;
import com.assignment_two_starter.repository.ProductRepository;
import com.assignment_two_starter.repository.WishlistRepository;
import com.google.zxing.WriterException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private QRCodeService qrCodeService;

    /**
     * Retrieves the currently authenticated customer from the security context.
     * @return Customer entity of the authenticated user.
     */
    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
    }

    @Transactional
    public WishlistItemDTO addItem(WishlistItemRequestDTO requestDTO) {
        Customer customer = getAuthenticatedCustomer();
        Product product = productRepository.findByName(requestDTO.getProductName())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + requestDTO.getProductName()));

        WishlistItem item = new WishlistItem();
        item.setCustomer(customer);
        item.setProduct(product);
        item.setNote(requestDTO.getNote());
        WishlistItem saved = wishlistRepository.save(item);


        // After adding the wishlist item, email the user.
        try {

            String token = jwtUtil.generateToken(customer.getEmail());
            // Retrieve the full wishlist for the customer
            List<WishlistItemDTO> wishlist = getWishlistForCustomer(customer);

            // URL will be changed once ngrok has been started up again
            // cmd: cd C:\Users\eivid\Downloads\ngrok-v3-stable-windows-amd64
            // cmd: ngrok http https://localhost:8443
            String publicUrl = "https://e35c-193-1-94-53.ngrok-free.app/public/wishlist?token=" + token;

            byte[] qrBytes = qrCodeService.generateQRCodeImage(publicUrl, 250, 250);
            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrBytes);

            String subject = "Your Wishlist Has Been Updated";

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("Dear ").append(customer.getFirstName()).append(",<br><br>")
                    .append("An item has been added to your wishlist. ")
                    .append("Please scan the QR code below or click the link to view it:<br><br>")
                    .append("<a href='").append(publicUrl).append("'>View Wishlist</a><br><br>")
                    .append("<img src='data:image/png;base64,").append(qrCodeBase64).append("' alt='QR Code' /><br><br>");

            String htmlContent = htmlBuilder.toString();

            // 5. Send the email
            notificationService.sendHtmlEmail(customer.getEmail(), subject, htmlContent);

        } catch (WriterException | IOException | MessagingException e) {
            e.printStackTrace();
        }

        return convertToDTO(saved);
    }

    @Transactional
    public WishlistItemDTO updateItem(Integer itemId, WishlistItemRequestDTO requestDTO) {
        WishlistItem item = wishlistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found with id: " + itemId));
        Customer customer = getAuthenticatedCustomer();
        if (!item.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new RuntimeException("Not authorized to update this wishlist item");
        }
        item.setNote(requestDTO.getNote());
        item.setUpdatedAt(new java.util.Date());
        WishlistItem updated = wishlistRepository.save(item);
        return convertToDTO(updated);
    }

    @Transactional
    public void removeItem(Integer itemId) {
        WishlistItem item = wishlistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found with id: " + itemId));
        Customer customer = getAuthenticatedCustomer();
        if (!item.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new RuntimeException("Not authorized to remove this wishlist item");
        }
        wishlistRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<WishlistItemDTO> getWishlist() {
        Customer customer = getAuthenticatedCustomer();
        List<WishlistItem> items = wishlistRepository.findByCustomer(customer);
        return items.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private WishlistItemDTO convertToDTO(WishlistItem item) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(item.getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductPrice(item.getProduct().getPrice());
        dto.setNote(item.getNote());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemDTO> getWishlistForCustomer(Customer customer) {
        List<WishlistItem> items = wishlistRepository.findByCustomer(customer);
        return items.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<WishlistItemDTO> getWishlistPage(int page, int size) {
        Customer customer = getAuthenticatedCustomer();
        Pageable pageable = PageRequest.of(page, size);
        Page<WishlistItem> wishlistPage = wishlistRepository.findByCustomer(customer, pageable);
        return wishlistPage.map(this::convertToDTO);
    }


}
