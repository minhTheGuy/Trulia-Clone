package com.ecommerce.property.payload.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedHomeDTO {
    private Long id;
    private Long userId;
    private Long propertyId;
    private LocalDateTime savedAt;
} 