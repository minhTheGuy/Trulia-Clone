package com.ecommerce.property.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.property.payload.dto.RentalDTO;
import com.ecommerce.property.payload.request.RentalRequest;
import com.ecommerce.property.service.RentalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties/rentals")
public class RentalController {
    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

    private final RentalService rentalService;

    @PostMapping
    public ResponseEntity<RentalDTO> createRental(@Valid @RequestBody RentalRequest rentalRequest) {
        logger.info("Creating new rental for property ID: {}", rentalRequest.getPropertyId());
        RentalDTO rental = rentalService.createRental(rentalRequest);
        return new ResponseEntity<>(rental, HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<RentalDTO>> getUserRentals(@PathVariable Long userId) {
        logger.info("Fetching rentals for user ID: {}", userId);
        List<RentalDTO> rentals = rentalService.getRentalsByUserId(userId);
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<List<RentalDTO>> getPropertyRentals(@PathVariable Long propertyId) {
        logger.info("Fetching rentals for property ID: {}", propertyId);
        List<RentalDTO> rentals = rentalService.getRentalsByPropertyId(propertyId);
        return ResponseEntity.ok(rentals);
    }

    @PutMapping("/{rentalId}/cancel")
    public ResponseEntity<RentalDTO> cancelRental(@PathVariable Long rentalId) {
        logger.info("Cancelling rental ID: {}", rentalId);
        RentalDTO rental = rentalService.cancelRental(rentalId);
        return ResponseEntity.ok(rental);
    }

    @PutMapping("/{rentalId}/extend")
    public ResponseEntity<RentalDTO> extendRental(
            @PathVariable Long rentalId,
            @Valid @RequestBody RentalRequest extensionRequest) {
        logger.info("Extending rental ID: {}", rentalId);
        RentalDTO rental = rentalService.extendRental(rentalId, extensionRequest);
        return ResponseEntity.ok(rental);
    }

    @GetMapping("/{rentalId}")
    public ResponseEntity<RentalDTO> getRentalDetails(@PathVariable Long rentalId) {
        logger.info("Fetching details for rental ID: {}", rentalId);
        RentalDTO rental = rentalService.getRentalById(rentalId);
        return ResponseEntity.ok(rental);
    }
} 