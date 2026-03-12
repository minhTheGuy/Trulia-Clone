package com.ecommerce.transaction.model;

public enum PaymentMethod {
    STRIPE,        // Credit card payment via Stripe
    BANK_TRANSFER, // Bank transfer
    CASH,          // Cash payment
    ESCROW         // Escrow payment
} 