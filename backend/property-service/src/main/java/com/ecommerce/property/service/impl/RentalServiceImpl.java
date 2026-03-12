package com.ecommerce.property.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.property.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.property.model.Property;
import com.ecommerce.property.model.Rental;
import com.ecommerce.property.payload.dto.RentalDTO;
import com.ecommerce.property.payload.request.RentalRequest;
import com.ecommerce.property.repository.PropertyRepository;
import com.ecommerce.property.repository.RentalRepository;
import com.ecommerce.property.service.RentalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public RentalDTO createRental(RentalRequest rentalRequest) {
        // Validate property exists
        Property property = propertyRepository.findById(rentalRequest.getPropertyId())
            .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + rentalRequest.getPropertyId()));

        // Create new rental
        Rental rental = Rental.builder()
            .property(property)
            .userId(rentalRequest.getUserId())
            .startDate(rentalRequest.getStartDate())
            .endDate(rentalRequest.getEndDate())
            .totalPrice(rentalRequest.getTotalPrice())
            .status("ACTIVE")
            .paymentStatus("PENDING")
            .createdAt(LocalDate.now())
            .build();

        // Save rental
        Rental savedRental = rentalRepository.save(rental);
        
        return mapToDTO(savedRental);
    }

    @Override
    public List<RentalDTO> getRentalsByUserId(Long userId) {
        List<Rental> rentals = rentalRepository.findByUserId(userId);
        return rentals.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RentalDTO> getRentalsByPropertyId(Long propertyId) {
        List<Rental> rentals = rentalRepository.findByPropertyId(propertyId);
        return rentals.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RentalDTO cancelRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + rentalId));
            
        rental.setStatus("CANCELLED");
        Rental updatedRental = rentalRepository.save(rental);
        
        return mapToDTO(updatedRental);
    }

    @Override
    public RentalDTO extendRental(Long rentalId, RentalRequest extensionRequest) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + rentalId));
            
        // Update rental period and price
        rental.setEndDate(extensionRequest.getEndDate());
        rental.setTotalPrice(extensionRequest.getTotalPrice());
        
        Rental updatedRental = rentalRepository.save(rental);
        return mapToDTO(updatedRental);
    }

    @Override
    public RentalDTO getRentalById(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + rentalId));
        return mapToDTO(rental);
    }

    // Helper method to map Rental entity to DTO
    private RentalDTO mapToDTO(Rental rental) {
        return RentalDTO.builder()
            .id(rental.getId())
            .propertyId(rental.getProperty().getId())
            .userId(rental.getUserId())
            .startDate(rental.getStartDate())
            .endDate(rental.getEndDate())
            .totalPrice(rental.getTotalPrice())
            .status(rental.getStatus())
            .paymentStatus(rental.getPaymentStatus())
            .createdAt(rental.getCreatedAt())
            .build();
    }
} 