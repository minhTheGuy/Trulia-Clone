package com.ecommerce.property.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.ecommerce.property.exceptions.ResourceNotFoundException;
import com.ecommerce.property.model.Property;
import com.ecommerce.property.model.PropertyTour;
import com.ecommerce.property.model.PropertyTour.TourStatus;
import com.ecommerce.property.model.PropertyTour.TourType;
import com.ecommerce.property.payload.dto.PropertyTourDTO;
import com.ecommerce.property.repository.PropertyRepository;
import com.ecommerce.property.repository.PropertyTourRepository;
import com.ecommerce.property.service.PropertyTourService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyTourServiceImpl implements PropertyTourService {

    private final PropertyTourRepository propertyTourRepository;
    private final PropertyRepository propertyRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public PropertyTourDTO scheduleTour(PropertyTourDTO tourDTO) {
        // Validate property exists
        Property property = propertyRepository.findById(tourDTO.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + tourDTO.getPropertyId()));
        
        // Convert DTO to entity
        PropertyTour tour = modelMapper.map(tourDTO, PropertyTour.class);
        
        // Set default values if not provided
        if (tour.getTourType() == null) {
            tour.setTourType(TourType.IN_PERSON);
        }
        
        tour.setStatus(TourStatus.PENDING);
        tour.setCreatedAt(LocalDateTime.now());
        tour.setUpdatedAt(LocalDateTime.now());
        
        // Save tour
        PropertyTour savedTour = propertyTourRepository.save(tour);
        
        // Convert back to DTO
        PropertyTourDTO savedTourDTO = modelMapper.map(savedTour, PropertyTourDTO.class);
        
        // Enrich DTO with property details
        savedTourDTO.setPropertyTitle(property.getTitle());
        savedTourDTO.setPropertyAddress(property.getAddress());
        
        return savedTourDTO;
    }

    @Override
    public PropertyTourDTO getTourById(Long id) {
        PropertyTour tour = propertyTourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        
        PropertyTourDTO tourDTO = modelMapper.map(tour, PropertyTourDTO.class);
        enrichTourDTO(tourDTO);
        
        return tourDTO;
    }

    @Override
    public List<PropertyTourDTO> getToursByUserId(Long userId) {
        List<PropertyTour> tours = propertyTourRepository.findByUserId(userId);
        return tours.stream()
                .map(tour -> {
                    PropertyTourDTO dto = modelMapper.map(tour, PropertyTourDTO.class);
                    enrichTourDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyTourDTO> getToursByPropertyId(Long propertyId) {
        List<PropertyTour> tours = propertyTourRepository.findByPropertyId(propertyId);
        return tours.stream()
                .map(tour -> {
                    PropertyTourDTO dto = modelMapper.map(tour, PropertyTourDTO.class);
                    enrichTourDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyTourDTO> getToursByUserIdAndStatus(Long userId, TourStatus status) {
        List<PropertyTour> tours = propertyTourRepository.findByUserIdAndStatus(userId, status);
        return tours.stream()
                .map(tour -> {
                    PropertyTourDTO dto = modelMapper.map(tour, PropertyTourDTO.class);
                    enrichTourDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyTourDTO> getToursByDateRange(LocalDate startDate, LocalDate endDate) {
        List<PropertyTour> tours = propertyTourRepository.findByTourDateBetween(startDate, endDate);
        return tours.stream()
                .map(tour -> {
                    PropertyTourDTO dto = modelMapper.map(tour, PropertyTourDTO.class);
                    enrichTourDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PropertyTourDTO updateTourStatus(Long id, TourStatus status) {
        PropertyTour tour = propertyTourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        
        tour.setStatus(status);
        tour.setUpdatedAt(LocalDateTime.now());
        
        PropertyTour updatedTour = propertyTourRepository.save(tour);
        
        PropertyTourDTO tourDTO = modelMapper.map(updatedTour, PropertyTourDTO.class);
        enrichTourDTO(tourDTO);
        
        return tourDTO;
    }

    @Override
    public void cancelTour(Long id) {
        PropertyTour tour = propertyTourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        
        tour.setStatus(TourStatus.CANCELLED);
        tour.setUpdatedAt(LocalDateTime.now());
        
        propertyTourRepository.save(tour);
    }
    
    /**
     * Helper method to enrich tour DTOs with related data
     */
    private void enrichTourDTO(PropertyTourDTO tourDTO) {
        try {
            Property property = propertyRepository.findById(tourDTO.getPropertyId())
                    .orElse(null);
            
            if (property != null) {
                tourDTO.setPropertyTitle(property.getTitle());
                tourDTO.setPropertyAddress(property.getAddress());
                
                // Set seller name from property info (previously was agent)
                // In a real app, this would come from a user service
                tourDTO.setUserSellerName(property.getAgentName()); // Using agentName field for now
            }
            
            // Note: In a real app, you would fetch and set user details as well
            // from a user service or repository
        } catch (Exception e) {
            // Log but don't fail if enrichment fails
        }
    }
} 