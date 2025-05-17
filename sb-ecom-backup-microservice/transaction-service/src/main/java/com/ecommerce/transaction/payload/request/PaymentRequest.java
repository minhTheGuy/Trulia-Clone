package com.ecommerce.transaction.payload.request;

import java.math.BigDecimal;

import com.ecommerce.transaction.model.PaymentMethod;

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
public class PaymentRequest {
    @NotNull(message = "Transaction ID is required")
    private Long transactionId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    // For Stripe payments
    private String stripeToken;
    private String stripeCustomerId;
    
    private String description;
    
    // For manual payments
    private String referenceNumber;
} 