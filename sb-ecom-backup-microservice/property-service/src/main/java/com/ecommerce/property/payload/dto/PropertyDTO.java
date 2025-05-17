package com.ecommerce.property.payload.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDTO {
    private Long id;
    private String type;
    private String title;
    private String address;
    private double price;
    private int bedrooms;
    private int bathrooms;
    private double sqft;
    private String lotSize;
    private int yearBuilt;
    private String description;
    private List<String> features;
    private List<String> images;
    private AgentDTO agent;
    private String status; // for_sale, for_rent, etc.
    private int daysOnMarket;
    private LocationDTO location;
    private NeighborhoodDTO neighborhood;
    private AddressDetailsDTO addressDetails;
    
    // Additional fields needed for backend functionality
    private Long categoryId;
    private String categoryName;
    private double rentPrice;
    private boolean forSale;
    private boolean forRent;
    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int totalWatched;
}