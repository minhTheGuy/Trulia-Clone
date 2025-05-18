package com.ecommerce.property.service;

import java.util.List;

import com.ecommerce.property.payload.dto.RentalDTO;
import com.ecommerce.property.payload.request.RentalRequest;

public interface RentalService {
    // Create a new rental
    RentalDTO createRental(RentalRequest rentalRequest);
    
    // Get rentals by user ID
    List<RentalDTO> getRentalsByUserId(Long userId);
    
    // Get rentals by property ID
    List<RentalDTO> getRentalsByPropertyId(Long propertyId);
    
    // Cancel a rental
    RentalDTO cancelRental(Long rentalId);
    
    // Extend a rental
    RentalDTO extendRental(Long rentalId, RentalRequest extensionRequest);
    
    // Get rental details by ID
    RentalDTO getRentalById(Long rentalId);
} 