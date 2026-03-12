package com.ecommerce.transaction.payload.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.TransactionType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RentalTransactionRequest {
    
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    // Allow buyerId to be null if userId is provided
    private Long buyerId;
    
    // Add userId field as alternative to buyerId for frontend compatibility
    private Long userId;
    
    @NotNull(message = "Seller ID is required")
    private Long sellerId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    private BigDecimal commission;
    
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Rental period is required")
    private String rentalPeriod;
    
    private String rentalStartDate;
    private String rentalEndDate;
    
    private String propertyTitle;
    private String propertyAddress;
    
    private String buyerName;
    private String sellerName;
    
    private String notes;
    
    // Custom validation to ensure either buyerId or userId is provided
    @AssertTrue(message = "Either buyerId or userId must be provided")
    private boolean isUserIdValid() {
        return buyerId != null || userId != null;
    }
    
    /**
     * Converts this rental request into a standard transaction request
     * @return TransactionRequest with populated rental information
     */
    public TransactionRequest toTransactionRequest() {
        TransactionRequest request = new TransactionRequest();
        
        // Set basic transaction information
        request.setPropertyId(this.propertyId);
        
        // Use userId as buyerId if buyerId is null
        Long effectiveBuyerId = this.buyerId != null ? this.buyerId : this.userId;
        request.setBuyerId(effectiveBuyerId);
        
        request.setSellerId(this.sellerId);
        request.setAmount(this.amount);
        request.setCommission(this.commission);
        request.setPaymentMethod(this.paymentMethod);
        request.setPropertyTitle(this.propertyTitle);
        request.setPropertyAddress(this.propertyAddress);
        request.setBuyerName(this.buyerName);
        request.setSellerName(this.sellerName);
        request.setNotes(this.notes);
        
        // Set rental-specific fields
        request.setTransactionType(TransactionType.RENT);
        request.setRentalPeriod(this.rentalPeriod);
        
        // Process rental dates
        try {
            LocalDateTime startDate = LocalDateTime.now();
            request.setRentalStartDate(startDate);
            
            // Calculate end date based on rental period
            if (this.rentalPeriod != null) {
                int months = Integer.parseInt(this.rentalPeriod);
                LocalDateTime endDate = startDate.plusMonths(months);
                request.setRentalEndDate(endDate);
            }
        } catch (Exception e) {
            // Handle parsing errors
        }
        
        return request;
    }
} 