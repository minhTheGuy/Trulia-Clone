package com.ecommerce.transaction.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.payload.dto.TransactionDTO;
import com.ecommerce.transaction.payload.request.RentalTransactionRequest;
import com.ecommerce.transaction.payload.request.TransactionRequest;
import com.ecommerce.transaction.payload.dto.TransactionStatsDTO;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;
import com.ecommerce.transaction.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("Creating new transaction for property ID: {}", request.getPropertyId());
        TransactionDTO transaction = transactionService.createTransaction(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }
    
    @PostMapping("/create-rental")
    public ResponseEntity<TransactionDTO> createRentalTransaction(@Valid @RequestBody RentalTransactionRequest rentalRequest) {
        log.info("Creating new rental transaction for property ID: {}", rentalRequest.getPropertyId());
        
        // Convert rental request to transaction request
        TransactionRequest request = rentalRequest.toTransactionRequest();
        
        TransactionDTO transaction = transactionService.createTransaction(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        log.info("Fetching transaction with ID: {}", id);
        TransactionDTO transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<PagedResponse<TransactionDTO>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status) {
        
        log.info("Fetching transactions for user ID: {}", userId);
        PagedResponse<TransactionDTO> transactions = transactionService.getAllTransactions(
                page, size, sortBy, sortDir, null, userId, null, status, type, null, null);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<PagedResponse<TransactionDTO>> getPropertyTransactions(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status) {
        
        log.info("Fetching transactions for property ID: {}", propertyId);
        PagedResponse<TransactionDTO> transactions = transactionService.getAllTransactions(
                page, size, sortBy, sortDir, propertyId, null, null, status, type, null, null);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping
    public ResponseEntity<PagedResponse<TransactionDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching all transactions with filters");
        PagedResponse<TransactionDTO> transactions = transactionService.getAllTransactions(
                page, size, sortBy, sortDir, propertyId, buyerId, sellerId, status, type, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id, 
            @Valid @RequestBody TransactionRequest request) {
        
        log.info("Updating transaction with ID: {}", id);
        TransactionDTO updatedTransaction = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(updatedTransaction);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<TransactionDTO> cancelTransaction(@PathVariable Long id) {
        log.info("Cancelling transaction with ID: {}", id);
        TransactionDTO cancelledTransaction = transactionService.cancelTransaction(id);
        return ResponseEntity.ok(cancelledTransaction);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionDTO> updateTransactionStatus(
            @PathVariable Long id, 
            @RequestParam TransactionStatus status) {
        
        log.info("Updating status of transaction ID: {} to {}", id, status);
        TransactionDTO updatedTransaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(updatedTransaction);
    }
    
    @PutMapping("/{id}/deposit-paid")
    public ResponseEntity<TransactionDTO> markDepositPaid(@PathVariable Long id) {
        log.info("Marking deposit paid for transaction ID: {}", id);
        TransactionDTO updatedTransaction = transactionService.markDepositPaid(id);
        return ResponseEntity.ok(updatedTransaction);
    }
    
    @PutMapping("/{id}/contract-signed")
    public ResponseEntity<TransactionDTO> markContractSigned(@PathVariable Long id) {
        log.info("Marking contract signed for transaction ID: {}", id);
        TransactionDTO updatedTransaction = transactionService.markContractSigned(id);
        return ResponseEntity.ok(updatedTransaction);
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<TransactionDTO> completeTransaction(@PathVariable Long id) {
        log.info("Completing transaction ID: {}", id);
        TransactionDTO completedTransaction = transactionService.markTransactionCompleted(id);
        return ResponseEntity.ok(completedTransaction);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<TransactionStatsDTO> getTransactionStats() {
        log.info("Fetching transaction statistics");
        TransactionStatsDTO stats = transactionService.getTransactionStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/reports/monthly")
    public ResponseEntity<PagedResponse<TransactionDTO>> getMonthlyTransactionReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching monthly transaction report for {}-{}", year, month);
        PagedResponse<TransactionDTO> report = transactionService.getMonthlyTransactionReport(year, month, page, size);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<PagedResponse<TransactionDTO>> getTransactionsBySellerId(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching transactions for seller ID: {}", sellerId);
        PagedResponse<TransactionDTO> transactions = transactionService.getAllTransactions(
                page, size, sortBy, sortDir, null, null, sellerId, null, null, null, null);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/revenue/statistics")
    public ResponseEntity<TransactionStatsDTO> getRevenueStatistics(
            @RequestParam String timeRange,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter) {

        log.info("Fetching revenue statistics for time range: {}", timeRange);
        TransactionStatsDTO stats = transactionService.getRevenueStatistics(timeRange, year, month, quarter);
        return ResponseEntity.ok(stats);
    }
} 