package com.ecommerce.transaction.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.payload.dto.TransactionDTO;
import com.ecommerce.transaction.payload.request.RentalExtensionRequest;
import com.ecommerce.transaction.payload.request.RentalRequest;
import com.ecommerce.transaction.payload.request.TransactionRequest;
import com.ecommerce.transaction.payload.dto.TransactionStatsDTO;
import com.ecommerce.transaction.exception.ResourceNotFoundException;
import com.ecommerce.transaction.exception.TransactionApiException;
import com.ecommerce.transaction.mapper.TransactionMapper;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;
import com.ecommerce.transaction.repository.TransactionRepository;
import com.ecommerce.transaction.service.NotificationService;
import com.ecommerce.transaction.service.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransactionMapper transactionMapper;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public TransactionDTO createTransaction(TransactionRequest request) {
        log.info("Creating new transaction for property ID: {}", request.getPropertyId());
        
        Transaction transaction = Transaction.builder()
                .propertyId(request.getPropertyId())
                .buyerId(request.getBuyerId())
                .sellerId(request.getSellerId())
                .amount(request.getAmount())
                .commission(request.getCommission())
                .transactionType(request.getTransactionType())
                .status(TransactionStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .contractNumber(request.getContractNumber())
                .propertyTitle(request.getPropertyTitle())
                .propertyAddress(request.getPropertyAddress())
                .buyerName(request.getBuyerName())
                .sellerName(request.getSellerName())
                .notes(request.getNotes())
                .depositPaid(false)
                .contractSigned(false)
                .fullPaymentCompleted(false)
                .rentalPeriod(request.getRentalPeriod())
                .rentalStartDate(request.getRentalStartDate())
                .rentalEndDate(request.getRentalEndDate())
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        // Send notification
        notificationService.sendTransactionCreatedNotification(transaction);
        
        return transactionMapper.mapToDTO(transaction);
    }
    
    @Override
    @Transactional
    public TransactionDTO createRental(RentalRequest request) {
        log.info("Creating new rental transaction for property ID: {}", request.getPropertyId());
        
        // Convert RentalRequest to TransactionRequest
        TransactionRequest transactionRequest = new TransactionRequest();
        
        // Set basic transaction information
        transactionRequest.setPropertyId(request.getPropertyId());
        
        // Use userId as buyerId if buyerId is null
        Long effectiveBuyerId = request.getBuyerId() != null ? request.getBuyerId() : request.getUserId();
        if (effectiveBuyerId == null) {
            throw new TransactionApiException(HttpStatus.BAD_REQUEST, "Either buyerId or userId must be provided");
        }
        transactionRequest.setBuyerId(effectiveBuyerId);
        
        transactionRequest.setSellerId(request.getSellerId());
        transactionRequest.setAmount(request.getAmount());
        transactionRequest.setPaymentMethod(request.getPaymentMethod());
        transactionRequest.setPropertyTitle(request.getPropertyTitle());
        transactionRequest.setPropertyAddress(request.getPropertyAddress());
        transactionRequest.setBuyerName(request.getBuyerName());
        transactionRequest.setSellerName(request.getSellerName());
        transactionRequest.setNotes(request.getNotes());
        
        // Set rental-specific fields
        transactionRequest.setTransactionType(TransactionType.RENT);
        transactionRequest.setRentalPeriod(request.getRentalPeriod());
        
        // Process rental dates
        try {
            LocalDateTime startDate = LocalDateTime.now();
            if (request.getRentalStartDate() != null && !request.getRentalStartDate().isEmpty()) {
                // In a real implementation, properly parse the date string
                // For simplicity, we're using current date
                startDate = LocalDateTime.now();
            }
            transactionRequest.setRentalStartDate(startDate);
            
            // Calculate end date based on rental period
            if (request.getRentalPeriod() != null) {
                int months = Integer.parseInt(request.getRentalPeriod());
                LocalDateTime endDate = startDate.plusMonths(months);
                transactionRequest.setRentalEndDate(endDate);
            }
        } catch (Exception e) {
            log.error("Error processing rental dates: {}", e.getMessage());
            throw new TransactionApiException(HttpStatus.BAD_REQUEST, "Invalid rental dates");
        }
        
        // Create the transaction
        return createTransaction(transactionRequest);
    }

    @Override
    public TransactionDTO getTransactionById(Long id) {
        log.info("Getting transaction with ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    public PagedResponse<TransactionDTO> getAllTransactions(
            int page, int size, String sortBy, String sortDir,
            Long propertyId, Long buyerId, Long sellerId,
            TransactionStatus status, TransactionType type,
            LocalDateTime startDate, LocalDateTime endDate) {
        
        log.info("Getting all transactions with filters");
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Transaction> transactions = transactionRepository.searchTransactions(
                propertyId, buyerId, sellerId, status, type, startDate, endDate, pageable);
        
        List<TransactionDTO> content = transactions.getContent().stream()
                .map(transactionMapper::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content, 
                transactions.getNumber(), 
                transactions.getSize(), 
                transactions.getTotalElements(), 
                transactions.getTotalPages(), 
                transactions.isLast());
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(Long id, TransactionRequest request) {
        log.info("Updating transaction with ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        // Check if transaction can be updated
        if (transaction.getStatus() == TransactionStatus.COMPLETED ||
            transaction.getStatus() == TransactionStatus.CANCELLED ||
            transaction.getStatus() == TransactionStatus.FAILED) {
            throw new TransactionApiException(HttpStatus.BAD_REQUEST, 
                    "Cannot update transaction with status: " + transaction.getStatus());
        }
        
        // Update fields
        transaction.setAmount(request.getAmount());
        transaction.setCommission(request.getCommission());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setContractNumber(request.getContractNumber());
        transaction.setPropertyTitle(request.getPropertyTitle());
        transaction.setPropertyAddress(request.getPropertyAddress());
        transaction.setBuyerName(request.getBuyerName());
        transaction.setSellerName(request.getSellerName());
        transaction.setNotes(request.getNotes());
        transaction.setRentalPeriod(request.getRentalPeriod());
        transaction.setRentalStartDate(request.getRentalStartDate());
        transaction.setRentalEndDate(request.getRentalEndDate());
        
        transaction = transactionRepository.save(transaction);
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionDTO cancelTransaction(Long id) {
        log.info("Cancelling transaction with ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        // Check if transaction can be cancelled
        if (transaction.getStatus() == TransactionStatus.COMPLETED ||
            transaction.getStatus() == TransactionStatus.CANCELLED ||
            transaction.getStatus() == TransactionStatus.FAILED) {
            throw new TransactionApiException(HttpStatus.BAD_REQUEST, 
                    "Cannot cancel transaction with status: " + transaction.getStatus());
        }
        
        String previousStatus = transaction.getStatus().toString();
        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction = transactionRepository.save(transaction);
        
        // Send notification
        notificationService.sendTransactionStatusChangedNotification(transaction, previousStatus);
        notificationService.sendTransactionCancelledNotification(transaction);
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionDTO updateTransactionStatus(Long id, TransactionStatus newStatus) {
        log.info("Updating status of transaction ID: {} to {}", id, newStatus);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        String previousStatus = transaction.getStatus().toString();
        transaction.setStatus(newStatus);
        
        if (newStatus == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
            transaction.setFullPaymentCompleted(true);
        }
        
        transaction = transactionRepository.save(transaction);
        
        // Send notification
        notificationService.sendTransactionStatusChangedNotification(transaction, previousStatus);
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionDTO markDepositPaid(Long id) {
        log.info("Marking deposit paid for transaction ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        transaction.setDepositPaid(true);
        transaction = transactionRepository.save(transaction);
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionDTO markContractSigned(Long id) {
        log.info("Marking contract signed for transaction ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        transaction.setContractSigned(true);
        transaction = transactionRepository.save(transaction);
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionDTO markTransactionCompleted(Long id) {
        log.info("Completing transaction ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setFullPaymentCompleted(true);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    public TransactionStatsDTO getTransactionStats() {
        log.info("Getting transaction statistics");
        
        // Get counts by status
        Map<TransactionStatus, Long> statusCounts = new HashMap<>();
        for (TransactionStatus status : TransactionStatus.values()) {
            long count = transactionRepository.countByStatus(status);
            statusCounts.put(status, count);
        }
        
        // Get counts by type
        Map<TransactionType, Long> typeCounts = new HashMap<>();
        for (TransactionType type : TransactionType.values()) {
            long count = transactionRepository.countByTransactionType(type);
            typeCounts.put(type, count);
        }
        
        // Get total amount
        BigDecimal totalAmount = transactionRepository.sumTotalAmount();
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        
        // Get completed transactions amount
        BigDecimal completedAmount = transactionRepository.sumCompletedAmount();
        if (completedAmount == null) {
            completedAmount = BigDecimal.ZERO;
        }
        
        // Get monthly stats for current year
        Map<Month, BigDecimal> monthlyStats = new HashMap<>();
        int currentYear = LocalDateTime.now().getYear();
        for (Month month : Month.values()) {
            BigDecimal amount = transactionRepository.sumAmountByYearAndMonth(currentYear, month.getValue());
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            monthlyStats.put(month, amount);
        }
        
        return TransactionStatsDTO.builder()
                .statusCounts(statusCounts)
                .typeCounts(typeCounts)
                .totalAmount(totalAmount)
                .completedAmount(completedAmount)
                .monthlyStats(monthlyStats)
                .build();
    }

    @Override
    public PagedResponse<TransactionDTO> getMonthlyTransactionReport(int year, int month, int page, int size) {
        log.info("Getting monthly transaction report for {}-{}", year, month);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate;
        if (month == 12) {
            endDate = LocalDateTime.of(year + 1, 1, 1, 0, 0).minusNanos(1);
        } else {
            endDate = LocalDateTime.of(year, month + 1, 1, 0, 0).minusNanos(1);
        }
        
        Page<Transaction> transactions = transactionRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        
        List<TransactionDTO> content = transactions.getContent().stream()
                .map(transactionMapper::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content, 
                transactions.getNumber(), 
                transactions.getSize(), 
                transactions.getTotalElements(), 
                transactions.getTotalPages(), 
                transactions.isLast());
    }
    
    @Override
    @Transactional
    public TransactionDTO extendRental(Long id, RentalExtensionRequest extensionRequest) {
        log.info("Extending rental transaction ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        
        // Check if this is a rental transaction
        if (transaction.getTransactionType() != TransactionType.RENT) {
            throw new TransactionApiException(HttpStatus.BAD_REQUEST, 
                    "Cannot extend non-rental transaction");
        }
        
        // Check if transaction can be extended
        if (transaction.getStatus() != TransactionStatus.COMPLETED && 
            transaction.getStatus() != TransactionStatus.IN_PROGRESS) {
            throw new TransactionApiException(HttpStatus.BAD_REQUEST, 
                    "Cannot extend rental with status: " + transaction.getStatus());
        }
        
        // Calculate new end date
        LocalDateTime currentEndDate = transaction.getRentalEndDate();
        if (currentEndDate == null) {
            currentEndDate = LocalDateTime.now();
        }
        
        LocalDateTime newEndDate = currentEndDate.plusMonths(extensionRequest.getAdditionalMonths());
        transaction.setRentalEndDate(newEndDate);
        
        // Update rental period
        String currentPeriod = transaction.getRentalPeriod();
        int currentMonths = 0;
        try {
            currentMonths = Integer.parseInt(currentPeriod);
        } catch (NumberFormatException e) {
            currentMonths = 0;
        }
        
        int newTotalMonths = currentMonths + extensionRequest.getAdditionalMonths();
        transaction.setRentalPeriod(String.valueOf(newTotalMonths));
        
        // Update amount
        BigDecimal newAmount = transaction.getAmount().add(extensionRequest.getAdditionalAmount());
        transaction.setAmount(newAmount);
        
        // Add note about extension
        String extensionNote = "Rental extended by " + extensionRequest.getAdditionalMonths() + 
                " months on " + LocalDateTime.now() + ". Additional amount: " + 
                extensionRequest.getAdditionalAmount();
        
        String currentNotes = transaction.getNotes();
        if (currentNotes == null || currentNotes.isEmpty()) {
            transaction.setNotes(extensionNote);
        } else {
            transaction.setNotes(currentNotes + "\n\n" + extensionNote);
        }
        
        transaction = transactionRepository.save(transaction);
        
        return transactionMapper.mapToDTO(transaction);
    }

    @Override
    public TransactionStatsDTO getRevenueStatistics(String timeRange, Integer year, Integer month, Integer quarter) {
        log.info("Getting revenue statistics for time range: {}, year: {}, month: {}, quarter: {}",
                timeRange, year, month, quarter);

        LocalDateTime startDate;
        LocalDateTime endDate;

        // Determine date range based on timeRange parameter
        switch (timeRange.toUpperCase()) {
            case "YEAR":
                startDate = LocalDateTime.of(year, 1, 1, 0, 0);
                endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
                break;
            case "QUARTER":
                if (quarter == null || quarter < 1 || quarter > 4) {
                    throw new TransactionApiException(HttpStatus.BAD_REQUEST, "Valid quarter (1-4) is required");
                }
                startDate = LocalDateTime.of(year, (quarter - 1) * 3 + 1, 1, 0, 0);
                endDate = quarter < 4
                        ? LocalDateTime.of(year, quarter * 3 + 1, 1, 0, 0).minusNanos(1)
                        : LocalDateTime.of(year, 12, 31, 23, 59, 59);
                break;
            case "MONTH":
                if (month == null || month < 1 || month > 12) {
                    throw new TransactionApiException(HttpStatus.BAD_REQUEST, "Valid month (1-12) is required");
                }
                startDate = LocalDateTime.of(year, month, 1, 0, 0);
                endDate = month < 12
                        ? LocalDateTime.of(year, month + 1, 1, 0, 0).minusNanos(1)
                        : LocalDateTime.of(year, 12, 31, 23, 59, 59);
                break;
            default:
                throw new TransactionApiException(HttpStatus.BAD_REQUEST,
                        "Invalid timeRange. Use MONTH, QUARTER, or YEAR");
        }

        // Get total revenue for all transactions in the period
        BigDecimal totalRevenue = transactionRepository.sumAmountByYearAndMonth(2025, 12);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // Get revenue for completed transactions in the period
        BigDecimal completedRevenue = transactionRepository.sumCompletedAmount();
        if (completedRevenue == null) {
            completedRevenue = BigDecimal.ZERO;
        }

        // Get transaction counts
        long totalTransactions = transactionRepository.countByCreatedAtBetween(startDate, endDate);
        long completedTransactions = transactionRepository.countByStatusAndCreatedAtBetween(
                TransactionStatus.COMPLETED, startDate, endDate);

        // Build and return the statistics DTO
        return TransactionStatsDTO.builder()
                .totalAmount(totalRevenue)
                .completedAmount(completedRevenue)
                .build();
    }
} 