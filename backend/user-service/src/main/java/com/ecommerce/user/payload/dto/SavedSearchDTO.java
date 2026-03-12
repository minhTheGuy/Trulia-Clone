package com.ecommerce.user.payload.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedSearchDTO {
    private Long id;
    private String name;
    private Object criteria;
    private LocalDateTime createdAt;
    private Long userId;
} 