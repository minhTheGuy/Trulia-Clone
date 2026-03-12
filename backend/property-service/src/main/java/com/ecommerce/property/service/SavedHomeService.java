package com.ecommerce.property.service;

import java.util.List;

import com.ecommerce.property.payload.dto.PropertyDTO;

public interface SavedHomeService {
    
    List<PropertyDTO> getSavedHomesByUserId(Long userId);
    
    PropertyDTO saveHome(Long userId, Long propertyId);
    
    void removeSavedHome(Long userId, Long propertyId);
    
    boolean isHomeSaved(Long userId, Long propertyId);
} 