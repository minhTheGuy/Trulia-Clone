package com.ecommerce.transaction.service;

import java.math.BigDecimal;
import java.util.Map;

import com.ecommerce.transaction.payload.request.RentalPaymentRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;

public interface StripeService {
    // Create a checkout session for rental payment
    Session createRentalCheckoutSession(RentalPaymentRequest request) throws StripeException;
    
    // Create a payment intent
    PaymentIntent createPaymentIntent(BigDecimal amount, String currency, Map<String, Object> metadata) throws StripeException;
    
    // Confirm a payment intent
    PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException;
    
    // Cancel a payment intent
    PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException;
    
    // Retrieve a payment intent
    PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException;
    
    // Create a refund
    void createRefund(String paymentIntentId, BigDecimal amount) throws StripeException;
    
    // Handle webhook event
    void handleWebhookEvent(String payload, String sigHeader) throws StripeException;
} 