package com.ecommerce.property.controller;

import com.ecommerce.property.payload.dto.PropertyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.property.config.AppConstants;
import com.ecommerce.property.payload.response.PropertyResponse;
import com.ecommerce.property.service.PropertyService;

import lombok.RequiredArgsConstructor;

/**
 * Controller for public (non-authenticated) property endpoints
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties/public")
public class PublicPropertyController {
    private static final Logger logger = LoggerFactory.getLogger(PublicPropertyController.class);

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<PropertyResponse> getAllProperties(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "price", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String category
    ) {
        logger.info("Public request for all properties");
        PropertyResponse propertyResponse = propertyService.getAllProperties(pageNumber, pageSize, sortBy, sortOrder, keyword, category);
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<PropertyResponse> getPropertiesByCategory(@PathVariable Long categoryId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "price", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        logger.info("Public request for properties in category ID: {}", categoryId);
        PropertyResponse propertyResponse = propertyService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }

    @GetMapping("/keyword")
    public ResponseEntity<PropertyResponse> getPropertiesByKeyword(@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "price", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        logger.info("Public request for properties matching keyword: {}", keyword);
        PropertyResponse propertyResponse = propertyService.searchPropertyByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PropertyResponse> searchProperties(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "price", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "minBedrooms", required = false) Integer minBedrooms,
            @RequestParam(name = "minBathrooms", required = false) Integer minBathrooms,
            @RequestParam(name = "minSquareFootage", required = false) Double minSquareFootage,
            @RequestParam(name = "propertyType", required = false) String propertyType,
            @RequestParam(name = "forSale", required = false) Boolean forSale,
            @RequestParam(name = "forRent", required = false) Boolean forRent
    ) {
        logger.info("Public request for filtered properties search");
        PropertyResponse propertyResponse = propertyService.searchPropertyByFilters(
                pageNumber, pageSize, sortBy, sortOrder, minPrice, maxPrice, minBedrooms,
                minBathrooms, minSquareFootage, propertyType, forSale, forRent);
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }

    @GetMapping("/location")
    public ResponseEntity<PropertyResponse> getPropertiesByLocation(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "zipcode", required = false) String zipcode,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "price", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        logger.info("Public request for properties by location - city: {}, state: {}, zipcode: {}",
                     city != null ? city : "n/a",
                     state != null ? state : "n/a",
                     zipcode != null ? zipcode : "n/a");
        PropertyResponse propertyResponse = propertyService.getPropertiesByLocation(
                city, state, zipcode, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(propertyResponse, HttpStatus.OK);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable Long propertyId) {
        logger.info("Public request for property with ID: {}", propertyId);
        PropertyDTO propertyDTO = propertyService.getPropertyById(propertyId);
        return new ResponseEntity<>(propertyDTO, HttpStatus.OK);
    }
} 