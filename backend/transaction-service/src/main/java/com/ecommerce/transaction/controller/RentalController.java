package com.ecommerce.transaction.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;
import com.ecommerce.transaction.payload.dto.TransactionDTO;
import com.ecommerce.transaction.payload.request.RentalExtensionRequest;
import com.ecommerce.transaction.payload.request.RentalRequest;
import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private static final Logger log = LoggerFactory.getLogger(RentalController.class);
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionDTO> rentProperty(@Valid @RequestBody RentalRequest request) {
        log.info("Creating new rental for property ID: {}", request.getPropertyId());
        
        // Convert rental request to transaction request with RENT type
        TransactionDTO transaction = transactionService.createRental(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }
    
    @GetMapping("/{rentalId}")
    public ResponseEntity<TransactionDTO> getRentalDetails(@PathVariable Long rentalId) {
        log.info("Fetching rental details with ID: {}", rentalId);
        TransactionDTO transaction = transactionService.getTransactionById(rentalId);
        
        // Verify this is a rental transaction
        if (transaction.getTransactionType() != TransactionType.RENT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<PagedResponse<TransactionDTO>> getUserRentals(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching rentals for user ID: {}", userId);
        PagedResponse<TransactionDTO> transactions = transactionService.getAllTransactions(
                page, size, sortBy, sortDir, null, userId, null, null, TransactionType.RENT, null, null);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<PagedResponse<TransactionDTO>> getPropertyRentals(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching rentals for property ID: {}", propertyId);
        PagedResponse<TransactionDTO> transactions = transactionService.getAllTransactions(
                page, size, sortBy, sortDir, propertyId, null, null, null, TransactionType.RENT, null, null);
        return ResponseEntity.ok(transactions);
    }
    
    @PutMapping("/{rentalId}/cancel")
    public ResponseEntity<TransactionDTO> cancelRental(@PathVariable Long rentalId) {
        log.info("Cancelling rental with ID: {}", rentalId);
        
        // First verify this is a rental transaction
        TransactionDTO transaction = transactionService.getTransactionById(rentalId);
        if (transaction.getTransactionType() != TransactionType.RENT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        
        // Cancel the transaction
        TransactionDTO cancelledTransaction = transactionService.updateTransactionStatus(rentalId, TransactionStatus.CANCELLED);
        return ResponseEntity.ok(cancelledTransaction);
    }
    
    @PutMapping("/{rentalId}/extend")
    public ResponseEntity<TransactionDTO> extendRental(
            @PathVariable Long rentalId,
            @Valid @RequestBody RentalExtensionRequest extensionRequest) {
        
        log.info("Extending rental with ID: {}", rentalId);
        
        // First verify this is a rental transaction
        TransactionDTO transaction = transactionService.getTransactionById(rentalId);
        if (transaction.getTransactionType() != TransactionType.RENT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        
        // Extend the rental
        TransactionDTO extendedRental = transactionService.extendRental(rentalId, extensionRequest);
        return ResponseEntity.ok(extendedRental);
    }
} 