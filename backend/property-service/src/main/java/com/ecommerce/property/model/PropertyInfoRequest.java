package com.ecommerce.property.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "property_info_requests")
public class PropertyInfoRequest {

    public enum RequestStatus {
        PENDING, RESPONDED, CLOSED
    }
    
    public enum ContactPreference {
        EMAIL, PHONE, BOTH
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long propertyId;
    
    private Long userId;
    
    private Long sellerUserId;
    
    private String name;
    
    private String email;
    
    private String phone;
    
    private String message;
    
    private String propertyTitle;
    
    @Enumerated(EnumType.STRING)
    private ContactPreference preferredContact = ContactPreference.EMAIL;
    
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 