package com.assignment_two_starter.dto;

import lombok.Data;

@Data
public class ShippingAddressDTO {
    private Integer addressId;
    private String streetAddress;
    private String city;
    private String county;
    private String postalCode;
    private String country;
}

