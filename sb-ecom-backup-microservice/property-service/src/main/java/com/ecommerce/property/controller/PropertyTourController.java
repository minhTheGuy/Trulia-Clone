package com.ecommerce.property.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.property.model.PropertyTour.TourStatus;
import com.ecommerce.property.payload.dto.PropertyTourDTO;
import com.ecommerce.property.service.PropertyTourService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for handling property tour scheduling
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties")
public class PropertyTourController {
    private static final Logger logger = LoggerFactory.getLogger(PropertyTourController.class);

    private final PropertyTourService propertyTourService;

    /**
     * Schedule a tour for a property
     */
    @PostMapping("/schedule-tour")
    public ResponseEntity<PropertyTourDTO> scheduleTour(@RequestBody PropertyTourDTO tourDTO) {
        logger.info("Scheduling tour for property ID: {}", tourDTO.getPropertyId());
        PropertyTourDTO scheduledTour = propertyTourService.scheduleTour(tourDTO);
        return new ResponseEntity<>(scheduledTour, HttpStatus.CREATED);
    }

    /**
     * Get a specific tour by ID
     */
    @GetMapping("/tours/{tourId}")
    public ResponseEntity<PropertyTourDTO> getTourById(@PathVariable Long tourId) {
        logger.info("Fetching tour with ID: {}", tourId);
        PropertyTourDTO tour = propertyTourService.getTourById(tourId);
        return ResponseEntity.ok(tour);
    }

    /**
     * Get all tours associated with a user (either as buyer or seller)
     */
    @GetMapping("/users/{userId}/tours")
    public ResponseEntity<List<PropertyTourDTO>> getToursByUserId(@PathVariable Long userId) {
        logger.info("Fetching tours for user ID: {}", userId);
        List<PropertyTourDTO> tours = propertyTourService.getToursByUserId(userId);
        return ResponseEntity.ok(tours);
    }

    /**
     * Get all tours for a specific property
     */
    @GetMapping("/{propertyId}/tours")
    public ResponseEntity<List<PropertyTourDTO>> getToursByPropertyId(@PathVariable Long propertyId) {
        logger.info("Fetching tours for property ID: {}", propertyId);
        List<PropertyTourDTO> tours = propertyTourService.getToursByPropertyId(propertyId);
        return ResponseEntity.ok(tours);
    }

    /**
     * Get tours for a user with specified status
     */
    @GetMapping("/users/{userId}/tours/status/{status}")
    public ResponseEntity<List<PropertyTourDTO>> getToursByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        TourStatus tourStatus = TourStatus.valueOf(status.toUpperCase());
        logger.info("Fetching tours for user ID: {} with status: {}", userId, tourStatus);
        List<PropertyTourDTO> tours = propertyTourService.getToursByUserIdAndStatus(userId, tourStatus);
        return ResponseEntity.ok(tours);
    }

    /**
     * Get tours scheduled between date range
     */
    @GetMapping("/tours/date-range")
    public ResponseEntity<List<PropertyTourDTO>> getToursByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("Fetching tours between dates: {} to {}", startDate, endDate);
        List<PropertyTourDTO> tours = propertyTourService.getToursByDateRange(startDate, endDate);
        return ResponseEntity.ok(tours);
    }

    /**
     * Update tour status
     */
    @PutMapping("/tours/{tourId}/status/{status}")
    public ResponseEntity<PropertyTourDTO> updateTourStatus(
            @PathVariable Long tourId,
            @PathVariable String status) {
        TourStatus tourStatus = TourStatus.valueOf(status.toUpperCase());
        logger.info("Updating tour ID: {} to status: {}", tourId, tourStatus);
        PropertyTourDTO updatedTour = propertyTourService.updateTourStatus(tourId, tourStatus);
        return ResponseEntity.ok(updatedTour);
    }

    /**
     * Cancel a tour
     */
    @DeleteMapping("/tours/{tourId}")
    public ResponseEntity<Void> cancelTour(@PathVariable Long tourId) {
        logger.info("Cancelling tour with ID: {}", tourId);
        propertyTourService.cancelTour(tourId);
        return ResponseEntity.noContent().build();
    }
} 