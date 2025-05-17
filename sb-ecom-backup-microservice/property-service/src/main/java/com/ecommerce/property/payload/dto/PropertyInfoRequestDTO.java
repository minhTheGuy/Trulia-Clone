package com.ecommerce.property.payload.dto;

import java.time.LocalDateTime;

import com.ecommerce.property.model.PropertyInfoRequest.ContactPreference;
import com.ecommerce.property.model.PropertyInfoRequest.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInfoRequestDTO {
    private Long id;
    private Long propertyId;
    private Long userId; // Requester (buyer) userId
    private Long sellerUserId; // Seller userId (previously agentId)
    private String name;
    private String email;
    private String phone;
    private String message;
    private String propertyTitle;
    private ContactPreference preferredContact;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for frontend
    private String propertyAddress;
    private String propertyImage;
    private String sellerName; // Previously agentName
    private String sellerEmail; // Previously agentEmail
    private String sellerPhone; // Previously agentPhone
} 