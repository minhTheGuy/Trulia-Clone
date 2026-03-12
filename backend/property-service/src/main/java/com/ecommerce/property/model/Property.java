package com.ecommerce.property.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "properties")
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // house, apartment, condo, etc.
    
    @NotBlank
    @Size(min = 3, message = "Property title must contain at least 3 characters")
    private String title;
    
    private String address; // Full address string for display
    
    private double price;
    private int bedrooms;
    private int bathrooms;
    private double sqft; // Square footage
    private String lotSize;
    private int yearBuilt;

    @Column(length = 2000)
    private String description;
    
    // Status information
    private String status; // for_sale, for_rent, sold, etc.
    private int daysOnMarket;
    
    // Location coordinates
    private double latitude;
    private double longitude;
    
    // Images
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "property_images", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "image_url")
    private List<String> images;
    
    // Features
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "property_features", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "feature")
    private List<String> features;
    
    // Agent information
    private String agentName;
    private String agentPhone;
    private String agentEmail;
    private String agentImage;
    private String agentCompany;
    
    // Neighborhood information
    private String neighborhoodName;

    @Column(columnDefinition = "text")
    private String neighborhoodDescription;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "neighborhood_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    private List<String> neighborhoodAmenities;
    
    private int walkScore;
    private int transitScore;
    private int bikeScore;
    
    // Detailed address information
    private String street;
    private String city;
    private String state;
    private String zip;
    
    // Additional fields for backend functionality
    private Long categoryId;
    private boolean forSale;
    private boolean forRent;
    private double rentPrice;
    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Track total views
    private int totalWatched = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
