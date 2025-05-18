package com.ecommerce.transaction.service;

import java.time.LocalDateTime;

import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.payload.dto.TransactionDTO;
import com.ecommerce.transaction.payload.request.RentalExtensionRequest;
import com.ecommerce.transaction.payload.request.RentalRequest;
import com.ecommerce.transaction.payload.request.TransactionRequest;
import com.ecommerce.transaction.payload.dto.TransactionStatsDTO;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;

public interface TransactionService {
    // Create a new transaction
    TransactionDTO createTransaction(TransactionRequest request);
    
    // Create a new rental transaction
    TransactionDTO createRental(RentalRequest request);
    
    // Get a transaction by ID
    TransactionDTO getTransactionById(Long id);
    
    // Get paginated transactions with filtering
    PagedResponse<TransactionDTO> getAllTransactions(
            int page, int size, String sortBy, String sortDir,
            Long propertyId, Long buyerId, Long sellerId,
            TransactionStatus status, TransactionType type,
            LocalDateTime startDate, LocalDateTime endDate);
    
    // Update a transaction
    TransactionDTO updateTransaction(Long id, TransactionRequest request);
    
    // Cancel a transaction
    TransactionDTO cancelTransaction(Long id);
    
    // Update transaction status
    TransactionDTO updateTransactionStatus(Long id, TransactionStatus newStatus);
    
    // Mark transaction as deposit paid
    TransactionDTO markDepositPaid(Long id);
    
    // Mark contract as signed
    TransactionDTO markContractSigned(Long id);
    
    // Mark transaction as completed
    TransactionDTO markTransactionCompleted(Long id);
    
    // Get transaction statistics
    TransactionStatsDTO getTransactionStats();
    
    // Get monthly transaction report
    PagedResponse<TransactionDTO> getMonthlyTransactionReport(int year, int month, int page, int size);
    
    // Extend a rental transaction
    TransactionDTO extendRental(Long id, RentalExtensionRequest extensionRequest);

    TransactionStatsDTO getRevenueStatistics(String timeRange, Integer year, Integer month, Integer quarter);
} 