package com.ecommerce.transaction.model;

public enum PaymentStatus {
    PENDING,     // Payment is pending
    PROCESSING,  // Payment is being processed
    COMPLETED,   // Payment is completed
    FAILED,      // Payment failed
    REFUNDED,    // Payment was refunded
    CANCELLED    // Payment was cancelled
} 