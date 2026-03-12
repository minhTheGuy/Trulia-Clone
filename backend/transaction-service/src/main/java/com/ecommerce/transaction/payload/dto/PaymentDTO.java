package com.ecommerce.transaction.payload.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private Long transactionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    
    private String stripePaymentIntentId;
    private String stripeCustomerId;
    private String referenceNumber;
    private String description;
    private String receiptUrl;
    
    // Client secret for Stripe payment intents - used by frontend to complete payment
    private String clientSecret;
    
    // Additional fields for response
    private String statusMessage;
    private String transactionType;
    private String propertyTitle;
} 