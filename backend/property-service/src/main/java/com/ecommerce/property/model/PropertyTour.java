package com.ecommerce.property.model;

import java.time.LocalDate;
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
@Table(name = "property_tours")
public class PropertyTour {

    public enum TourStatus {
        PENDING, CONFIRMED, COMPLETED, CANCELLED
    }
    
    public enum TourType {
        IN_PERSON, VIDEO
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long propertyId;
    
    private Long userId;
    
    private LocalDate tourDate;
    
    private String tourTime;
    
    @Enumerated(EnumType.STRING)
    private TourType tourType = TourType.IN_PERSON;
    
    private String contactPhone;
    
    private String message;
    
    @Enumerated(EnumType.STRING)
    private TourStatus status = TourStatus.PENDING;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 