package com.ecommerce.property.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.property.exceptions.ResourceNotFoundException;
import com.ecommerce.property.model.Property;
import com.ecommerce.property.model.SavedHome;
import com.ecommerce.property.payload.dto.PropertyDTO;
import com.ecommerce.property.repository.PropertyRepository;
import com.ecommerce.property.repository.SavedHomeRepository;
import com.ecommerce.property.service.SavedHomeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavedHomeServiceImpl implements SavedHomeService {

    private final SavedHomeRepository savedHomeRepository;
    private final PropertyRepository propertyRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PropertyDTO> getSavedHomesByUserId(Long userId) {
        List<SavedHome> savedHomes = savedHomeRepository.findByUserId(userId);
        
        if (savedHomes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> propertyIds = savedHomes.stream()
                .map(SavedHome::getPropertyId)
                .collect(Collectors.toList());
        
        List<Property> properties = propertyRepository.findAllById(propertyIds);
        
        return properties.stream()
                .map(property -> modelMapper.map(property, PropertyDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public PropertyDTO saveHome(Long userId, Long propertyId) {
        // Check if property exists
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        
        // Check if already saved
        if (!savedHomeRepository.existsByUserIdAndPropertyId(userId, propertyId)) {
            SavedHome savedHome = new SavedHome();
            savedHome.setUserId(userId);
            savedHome.setPropertyId(propertyId);
            savedHomeRepository.save(savedHome);
        }
        
        return modelMapper.map(property, PropertyDTO.class);
    }

    @Override
    @Transactional
    public void removeSavedHome(Long userId, Long propertyId) {
        // Check if property exists
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found with id: " + propertyId);
        }
        
        SavedHome savedHome = savedHomeRepository.findByUserIdAndPropertyId(userId, propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved home not found for user id: " + userId + 
                        " and property id: " + propertyId));
        
        savedHomeRepository.delete(savedHome);
    }

    @Override
    public boolean isHomeSaved(Long userId, Long propertyId) {
        return savedHomeRepository.existsByUserIdAndPropertyId(userId, propertyId);
    }
} 