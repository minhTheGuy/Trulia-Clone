package com.ecommerce.transaction.payload.request;

import java.math.BigDecimal;

import com.ecommerce.transaction.model.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequest {
    
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    // Allow either userId or buyerId
    private Long userId;
    private Long buyerId;
    
    @NotNull(message = "Seller ID is required")
    private Long sellerId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Rental period is required")
    private String rentalPeriod;
    
    private String rentalStartDate;
    private String rentalEndDate;
    
    // Optional details
    private String propertyTitle;
    private String propertyAddress;
    private String buyerName;
    private String sellerName;
    private String paymentId;
    private String transactionId;
    private String notes;
} 