package com.assignment_two_starter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistItemRequestDTO {
    @NotNull(message="Product name is required")
    private String productName;

    private String note;
}

