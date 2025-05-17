package com.ecommerce.property.payload.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeighborhoodDTO {
    private String name;
    private String description;
    private List<String> amenities;
    private int walkScore;
    private int transitScore;
    private int bikeScore;
} 