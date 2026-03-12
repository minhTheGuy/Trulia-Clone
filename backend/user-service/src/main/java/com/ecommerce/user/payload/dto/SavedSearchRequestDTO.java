package com.ecommerce.user.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedSearchRequestDTO {
    @NotBlank(message = "Search name is required")
    private String name;
    
    @NotNull(message = "Search criteria is required")
    private Object criteria;
} 