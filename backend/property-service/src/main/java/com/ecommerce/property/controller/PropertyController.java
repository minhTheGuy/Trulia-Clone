package com.ecommerce.property.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.property.payload.dto.PropertyDTO;
import com.ecommerce.property.service.PropertyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for authenticated/protected property endpoints
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties")
public class PropertyController {
    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    private final PropertyService propertyService;

    @PostMapping("/create")
    public ResponseEntity<PropertyDTO> createProperty(@Valid @RequestBody PropertyDTO propertyDTO) {
        logger.info("Creating new property with category ID: {}", propertyDTO.getCategoryId());

        PropertyDTO savedPropertyDTO = propertyService.addProperty(propertyDTO.getCategoryId(), propertyDTO);
        return new ResponseEntity<>(savedPropertyDTO, HttpStatus.CREATED);
    }

    @PostMapping("/categories/{categoryId}/property")
    public ResponseEntity<PropertyDTO> addProperty(@Valid @RequestBody PropertyDTO propertyDTO,
            @PathVariable Long categoryId) {
        logger.info("Creating new property in category ID: {}", categoryId);
        PropertyDTO savedPropertyDTO = propertyService.addProperty(categoryId, propertyDTO);
        return new ResponseEntity<>(savedPropertyDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertyDTO> updateProperty(@Valid @RequestBody PropertyDTO propertyDTO,
            @PathVariable Long propertyId) {
        logger.info("Updating property with ID: {}", propertyId);
        PropertyDTO updatedPropertyDTO = propertyService.updateProperty(propertyId, propertyDTO);
        return ResponseEntity.ok(updatedPropertyDTO);
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<PropertyDTO> deleteProperty(@PathVariable Long propertyId) {
        logger.info("Deleting property with ID: {}", propertyId);
        PropertyDTO deletedProperty = propertyService.deleteProperty(propertyId);
        return ResponseEntity.ok(deletedProperty);
    }

    @PutMapping("/{propertyId}/images")
    public ResponseEntity<PropertyDTO> updatePropertyImages(@PathVariable Long propertyId,
            @RequestParam("images") MultipartFile[] images) throws IOException {
        logger.info("Updating images for property with ID: {}", propertyId);
        PropertyDTO updatedProperty = propertyService.updatePropertyImages(propertyId, images);
        return ResponseEntity.ok(updatedProperty);
    }
    
    @GetMapping("/seller/{userId}")
    public ResponseEntity<List<PropertyDTO>> getPropertiesBySellerId(@PathVariable Long userId) {
        logger.info("Fetching properties for seller with user ID: {}", userId);
        List<PropertyDTO> properties = propertyService.getPropertiesBySellerId(userId);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/{id}/views")
    public ResponseEntity<Integer> getPropertyViews(@PathVariable Long id) {
        PropertyDTO property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property.getTotalWatched());
    }

    @PostMapping("/{id}/increment-view")
    public ResponseEntity<Integer> incrementPropertyViews(@PathVariable Long id) {
        PropertyDTO property = propertyService.incrementPropertyViews(id);
        return ResponseEntity.ok(property.getTotalWatched());
    }
}
