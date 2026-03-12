package com.ecommerce.property.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetailsDTO {
    private String street;
    private String city;
    private String state;
    private String zip;
    private String neighborhood;
} 