package com.ecommerce.transaction.payload.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;
    private Long propertyId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal amount;
    private BigDecimal commission;
    private TransactionType transactionType;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    
    private PaymentMethod paymentMethod;
    private String contractNumber;
    
    private String propertyTitle;
    private String propertyAddress;
    
    private String buyerName;
    private String sellerName;
    
    private String notes;
    
    private boolean depositPaid;
    private boolean contractSigned;
    private boolean fullPaymentCompleted;
    
    private String rentalPeriod;
    private LocalDateTime rentalStartDate;
    private LocalDateTime rentalEndDate;
    
    // Additional fields for response
    private String statusMessage;
    private boolean canBeCancelled;
} 