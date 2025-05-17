package com.ecommerce.user.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleDTO {
    private boolean isSellerRole; // True to add ROLE_SELLER, false to remove it
    private boolean isBrokerRole; // True to add ROLE_BROKER, false to remove it
} 