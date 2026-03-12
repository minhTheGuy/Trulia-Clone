package com.ecommerce.transaction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {

    private String eventType;

    private Long transactionId;
    private Long paymentId;
    private Long propertyId;
    private Long buyerId;
    private Long sellerId;

    private BigDecimal amount;
    private String currency;

    private String transactionType;
    private String transactionStatus;
    private String paymentStatus;

    private String stripePaymentIntentId;

    private LocalDateTime timestamp;
}
