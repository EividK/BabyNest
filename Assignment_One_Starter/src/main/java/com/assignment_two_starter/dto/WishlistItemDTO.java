package com.assignment_two_starter.dto;

import lombok.Data;

@Data
public class WishlistItemDTO {
    private Integer id;
    private String productName;
    private Double productPrice;
    private String note;
}

