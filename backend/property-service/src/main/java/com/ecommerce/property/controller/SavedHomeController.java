package com.ecommerce.property.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.property.payload.dto.PropertyDTO;
import com.ecommerce.property.service.SavedHomeService;

import lombok.RequiredArgsConstructor;

/**
 * Controller for saved homes endpoints
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties")
public class SavedHomeController {
    private static final Logger logger = LoggerFactory.getLogger(SavedHomeController.class);

    private final SavedHomeService savedHomeService;

    /**
     * Get all saved homes for a user
     */
    @GetMapping("/users/{userId}/saved-homes")
    public ResponseEntity<List<PropertyDTO>> getSavedHomes(@PathVariable Long userId) {
        logger.info("Fetching saved homes for user ID: {}", userId);
        List<PropertyDTO> savedHomes = savedHomeService.getSavedHomesByUserId(userId);
        return ResponseEntity.ok(savedHomes);
    }

    /**
     * Save a property to user's saved homes
     */
    @PostMapping("/users/{userId}/saved-homes/{propertyId}")
    public ResponseEntity<PropertyDTO> saveHome(@PathVariable Long userId, @PathVariable Long propertyId) {
        logger.info("Saving property ID: {} for user ID: {}", propertyId, userId);
        PropertyDTO savedProperty = savedHomeService.saveHome(userId, propertyId);
        return new ResponseEntity<>(savedProperty, HttpStatus.CREATED);
    }

    /**
     * Remove a property from user's saved homes
     */
    @DeleteMapping("/users/{userId}/saved-homes/{propertyId}")
    public ResponseEntity<Void> removeSavedHome(@PathVariable Long userId, @PathVariable Long propertyId) {
        logger.info("Removing property ID: {} from saved homes for user ID: {}", propertyId, userId);
        savedHomeService.removeSavedHome(userId, propertyId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a property is saved by a user
     */
    @GetMapping("/users/{userId}/saved-homes/{propertyId}/check")
    public ResponseEntity<Boolean> isHomeSaved(@PathVariable Long userId, @PathVariable Long propertyId) {
        logger.info("Checking if property ID: {} is saved by user ID: {}", propertyId, userId);
        boolean isSaved = savedHomeService.isHomeSaved(userId, propertyId);
        return ResponseEntity.ok(isSaved);
    }
} 