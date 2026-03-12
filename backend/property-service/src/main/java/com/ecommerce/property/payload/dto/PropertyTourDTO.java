package com.ecommerce.property.payload.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ecommerce.property.model.PropertyTour.TourStatus;
import com.ecommerce.property.model.PropertyTour.TourType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTourDTO {
    private Long id;
    private Long propertyId;
    private Long userId;
    private LocalDate tourDate;
    private String tourTime;
    private TourType tourType;
    private String contactPhone;
    private String message;
    private TourStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields that might be useful for the frontend
    private String propertyTitle;
    private String propertyAddress;
    private String userSellerName;
    private String userName;
} 