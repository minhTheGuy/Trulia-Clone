package com.ecommerce.transaction.model;

public enum TransactionStatus {
    PENDING,        // Transaction initiated but not completed
    DEPOSIT_PAID,   // Deposit has been paid
    CONTRACT_SIGNED, // Contract has been signed
    IN_PROGRESS,    // Transaction is in progress
    COMPLETED,      // Transaction completed successfully
    CANCELLED,      // Transaction cancelled
    FAILED,         // Transaction failed
    REFUNDED        // Transaction refunded
} 