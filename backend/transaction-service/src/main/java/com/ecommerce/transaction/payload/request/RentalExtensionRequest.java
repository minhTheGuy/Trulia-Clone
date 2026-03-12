package com.ecommerce.transaction.payload.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalExtensionRequest {
    
    @NotNull(message = "Additional months are required")
    @Positive(message = "Additional months must be positive")
    private Integer additionalMonths;
    
    @NotNull(message = "Additional amount is required")
    @Positive(message = "Additional amount must be positive")
    private BigDecimal additionalAmount;
    
    // Optional payment information
    private String paymentMethodId;
    private String paymentId;
} 