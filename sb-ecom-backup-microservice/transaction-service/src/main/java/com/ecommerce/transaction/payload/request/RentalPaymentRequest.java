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
public class RentalPaymentRequest {
    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    private Long propertyId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    // For Stripe payments
    private String stripeToken;
    private String stripeCustomerId;
    
    // For rental-specific information
    private String rentalPeriod;
    private String rentalStartDate;
    private String rentalEndDate;
    
    private String description;
    
    // For manual payments
    private String referenceNumber;
    
    // Convert to PaymentRequest
    public PaymentRequest toPaymentRequest() {
        return PaymentRequest.builder()
                .transactionId(this.transactionId)
                .amount(this.amount)
                .paymentMethod(this.paymentMethod)
                .stripeToken(this.stripeToken)
                .stripeCustomerId(this.stripeCustomerId)
                .description(this.description)
                .referenceNumber(this.referenceNumber)
                .build();
    }

}