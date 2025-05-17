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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.property.model.PropertyInfoRequest.RequestStatus;
import com.ecommerce.property.payload.dto.PropertyInfoRequestDTO;
import com.ecommerce.property.service.PropertyInfoRequestService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for handling property information requests
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties")
public class PropertyInfoRequestController {
    private static final Logger logger = LoggerFactory.getLogger(PropertyInfoRequestController.class);

    private final PropertyInfoRequestService infoRequestService;

    /**
     * Create a new information request
     */
    @PostMapping("/request-info")
    public ResponseEntity<PropertyInfoRequestDTO> createInfoRequest(@RequestBody PropertyInfoRequestDTO requestDTO) {
        logger.info("Creating information request for property ID: {}", requestDTO.getPropertyId());
        PropertyInfoRequestDTO createdRequest = infoRequestService.createInfoRequest(requestDTO);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    /**
     * Get a specific information request by ID
     */
    @GetMapping("/info-requests/{requestId}")
    public ResponseEntity<PropertyInfoRequestDTO> getInfoRequestById(@PathVariable Long requestId) {
        logger.info("Fetching information request with ID: {}", requestId);
        PropertyInfoRequestDTO request = infoRequestService.getInfoRequestById(requestId);
        return ResponseEntity.ok(request);
    }

    /**
     * Get all information requests made by a user (as a buyer)
     */
    @GetMapping("/users/{userId}/info-requests")
    public ResponseEntity<List<PropertyInfoRequestDTO>> getInfoRequestsByUserId(@PathVariable Long userId) {
        logger.info("Fetching information requests for user ID: {}", userId);
        List<PropertyInfoRequestDTO> requests = infoRequestService.getInfoRequestsByUserId(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get all information requests for properties owned by a seller
     */
    @GetMapping("/sellers/{sellerUserId}/info-requests")
    public ResponseEntity<List<PropertyInfoRequestDTO>> getInfoRequestsBySellerUserId(@PathVariable Long sellerUserId) {
        logger.info("Fetching information requests for seller user ID: {}", sellerUserId);
        List<PropertyInfoRequestDTO> requests = infoRequestService.getInfoRequestsBySellerUserId(sellerUserId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get all information requests for a specific property
     */
    @GetMapping("/{propertyId}/info-requests")
    public ResponseEntity<List<PropertyInfoRequestDTO>> getInfoRequestsByPropertyId(@PathVariable Long propertyId) {
        logger.info("Fetching information requests for property ID: {}", propertyId);
        List<PropertyInfoRequestDTO> requests = infoRequestService.getInfoRequestsByPropertyId(propertyId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get information requests for a user with specified status
     */
    @GetMapping("/users/{userId}/info-requests/status/{status}")
    public ResponseEntity<List<PropertyInfoRequestDTO>> getInfoRequestsByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
        logger.info("Fetching information requests for user ID: {} with status: {}", userId, requestStatus);
        List<PropertyInfoRequestDTO> requests = infoRequestService.getInfoRequestsByUserIdAndStatus(userId, requestStatus);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get information requests for a seller with specified status
     */
    @GetMapping("/sellers/{sellerUserId}/info-requests/status/{status}")
    public ResponseEntity<List<PropertyInfoRequestDTO>> getInfoRequestsBySellerUserIdAndStatus(
            @PathVariable Long sellerUserId,
            @PathVariable String status) {
        RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
        logger.info("Fetching information requests for seller user ID: {} with status: {}", sellerUserId, requestStatus);
        List<PropertyInfoRequestDTO> requests = infoRequestService.getInfoRequestsBySellerUserIdAndStatus(sellerUserId, requestStatus);
        return ResponseEntity.ok(requests);
    }

    /**
     * Update information request status
     */
    @PutMapping("/info-requests/{requestId}/status/{status}")
    public ResponseEntity<PropertyInfoRequestDTO> updateRequestStatus(
            @PathVariable Long requestId,
            @PathVariable String status) {
        RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
        logger.info("Updating information request ID: {} to status: {}", requestId, requestStatus);
        PropertyInfoRequestDTO updatedRequest = infoRequestService.updateRequestStatus(requestId, requestStatus);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Respond to an information request
     */
    @PutMapping("/info-requests/{requestId}/respond")
    public ResponseEntity<PropertyInfoRequestDTO> respondToRequest(
            @PathVariable Long requestId,
            @RequestBody String response) {
        logger.info("Responding to information request ID: {}", requestId);
        PropertyInfoRequestDTO updatedRequest = infoRequestService.respondToRequest(requestId, response);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Delete an information request
     */
    @DeleteMapping("/info-requests/{requestId}")
    public ResponseEntity<Void> deleteInfoRequest(@PathVariable Long requestId) {
        logger.info("Deleting information request with ID: {}", requestId);
        infoRequestService.deleteInfoRequest(requestId);
        return ResponseEntity.noContent().build();
    }
} 