package com.ecommerce.property.service;

import java.time.LocalDate;
import java.util.List;

import com.ecommerce.property.model.PropertyTour.TourStatus;
import com.ecommerce.property.payload.dto.PropertyTourDTO;

public interface PropertyTourService {
    
    PropertyTourDTO scheduleTour(PropertyTourDTO tourDTO);
    
    PropertyTourDTO getTourById(Long id);
    
    List<PropertyTourDTO> getToursByUserId(Long userId);
    
    List<PropertyTourDTO> getToursByPropertyId(Long propertyId);
    
    List<PropertyTourDTO> getToursByUserIdAndStatus(Long userId, TourStatus status);
    
    List<PropertyTourDTO> getToursByDateRange(LocalDate startDate, LocalDate endDate);
    
    PropertyTourDTO updateTourStatus(Long id, TourStatus status);
    
    void cancelTour(Long id);
} 