package com.ecommerce.transaction.payload.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    @NotNull(message = "Buyer ID is required")
    private Long buyerId;
    
    @NotNull(message = "Seller ID is required")
    private Long sellerId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private BigDecimal commission;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    private PaymentMethod paymentMethod;
    private String contractNumber;
    
    private String propertyTitle;
    private String propertyAddress;
    
    private String buyerName;
    private String sellerName;
    
    private String notes;
    
    // For rental transactions
    private String rentalPeriod;
    private LocalDateTime rentalStartDate;
    private LocalDateTime rentalEndDate;
} 