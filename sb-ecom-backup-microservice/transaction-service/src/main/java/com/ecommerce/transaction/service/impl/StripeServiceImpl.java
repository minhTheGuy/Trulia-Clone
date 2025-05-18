package com.ecommerce.transaction.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecommerce.transaction.payload.request.RentalPaymentRequest;
import com.ecommerce.transaction.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class StripeServiceImpl implements StripeService {
    private static final Logger log = LoggerFactory.getLogger(StripeServiceImpl.class);
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${frontend.success.url}")
    private String successUrl;

    @Value("${frontend.cancel.url}")
    private String cancelUrl;

    @Autowired
    private String stripeApiKey;
    
    public StripeServiceImpl(@Value("${stripe.secret.key}") String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey;
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public Session createRentalCheckoutSession(RentalPaymentRequest request) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        // Add rental payment line item
        lineItems.add(SessionCreateParams.LineItem.builder()
            .setPriceData(
                SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("vnd")
                    .setUnitAmount(request.getAmount().longValue()) // Amount in smallest currency unit (e.g., cents)
                    .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Rental Payment")
                            .setDescription(String.format("Rental payment for %s months", request.getRentalPeriod()))
                            .build()
                    )
                    .build()
            )
            .setQuantity(1L)
            .build()
        );

        // Add metadata for the session
        Map<String, String> metadata = new HashMap<>();
        metadata.put("transactionId", request.getTransactionId().toString());
        metadata.put("propertyId", request.getPropertyId().toString());
        metadata.put("rentalPeriod", request.getRentalPeriod());
        metadata.put("rentalStartDate", request.getRentalStartDate());
        metadata.put("rentalEndDate", request.getRentalEndDate());

        // Create checkout session
        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(cancelUrl)
            .addAllLineItem(lineItems)
            .putAllMetadata(metadata)
            .build();

        return Session.create(params);
    }

    @Override
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, Map<String, Object> metadata) throws StripeException {
        log.info("Creating payment intent for amount: {} {}", amount, currency);

        long amountInSmallestUnit;
        if (currency.equalsIgnoreCase("vnd")) {
            // VND is a zero-decimal currency, don't multiply by 100
            amountInSmallestUnit = amount.longValue();
        } else {
            // For currencies with decimal places (USD, EUR, etc.)
            amountInSmallestUnit = amount.multiply(new BigDecimal(100)).longValue();
        }

        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountInSmallestUnit)
                .setCurrency(currency.toLowerCase())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                );

        PaymentIntentCreateParams params = paramsBuilder.build();

        // Create the payment intent
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Add metadata if provided
        if (metadata != null && !metadata.isEmpty()) {
            Map<String, String> stripeMetadata = new HashMap<>();
            metadata.forEach((key, value) -> stripeMetadata.put(key, String.valueOf(value)));

            PaymentIntent.retrieve(paymentIntent.getId())
                    .update(Map.of("metadata", stripeMetadata));
        }

        log.info("Created payment intent: {}", paymentIntent.getId());
        return paymentIntent;
    }

    @Override
    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Confirming payment intent: {}", paymentIntentId);
        
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        paymentIntent = paymentIntent.confirm();
        
        log.info("Payment intent confirmed: {}", paymentIntentId);
        return paymentIntent;
    }

    @Override
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Cancelling payment intent: {}", paymentIntentId);
        
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        paymentIntent = paymentIntent.cancel();
        
        log.info("Payment intent cancelled: {}", paymentIntentId);
        return paymentIntent;
    }

    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Retrieving payment intent: {}", paymentIntentId);
        
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        log.info("Retrieved payment intent: {}", paymentIntentId);
        return paymentIntent;
    }

    @Override
    public void createRefund(String paymentIntentId, BigDecimal amount) throws StripeException {
        log.info("Creating refund for payment intent: {}", paymentIntentId);
        
        // Convert BigDecimal to cents for Stripe
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();
        
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amountInCents)
                .build();
        
        Refund refund = Refund.create(params);
        
        log.info("Created refund: {} for payment intent: {}", refund.getId(), paymentIntentId);
    }

    @Override
    public void handleWebhookEvent(String payload, String sigHeader) throws StripeException {
        log.info("Handling Stripe webhook event");
        
        Event event;
        
        // Verify webhook signature
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to process webhook", e);
            throw new StripeException(e.getMessage(), null, null, null) {};
        }
        
        log.info("Successfully verified webhook signature, event type: {}", event.getType());
        
        // Handle the event based on type
        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    // For simplicity, retrieve the payment intent directly using the API
                    // This avoids any deserialization issues
                    handlePaymentIntentEvent(event, true);
                    break;
                    
                case "payment_intent.payment_failed":
                    handlePaymentIntentEvent(event, false);
                    break;
                    
                default:
                    log.info("Unhandled event type: {}", event.getType());
                    break;
            }
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", e.getMessage(), e);
            throw new StripeException("Error processing webhook event: " + e.getMessage(), 
                    null, null, null) {};
        }
    }
    
    // Handle payment intent events (success or failure)
    private void handlePaymentIntentEvent(Event event, boolean isSuccessful) throws StripeException {
        // Extract the payment intent ID using gson to parse the JSON
        // This avoids the deserialization issues
        String jsonData = event.getDataObjectDeserializer().getRawJson();
        
        // Extract the payment intent ID from the raw data
        // The actual implementation depends on your Gson usage and JSON structure
        // For simplicity, retrieve the payment intent directly 
        String eventObjectId = event.getId();
        log.info("Processing payment intent event: {}, success: {}", eventObjectId, isSuccessful);
        
        // Logic that would normally handle successful or failed payments
        // For example, find the transaction in your database by Stripe payment intent ID
        // and update its status accordingly
    }
    
    // Helper methods to handle different event types
    private void handlePaymentIntentSucceeded(String paymentIntentId) throws StripeException {
        // Retrieve full payment intent data if needed
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        // Update payment status in your database
        log.info("Processing successful payment: {}", paymentIntentId);
        
        // Find associated payment in our system
        // Logic to update payment status, mark transaction as paid, etc.
    }
    
    private void handlePaymentIntentFailed(String paymentIntentId) throws StripeException {
        // Retrieve full payment intent data if needed
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        // Update payment status in your database
        log.info("Processing failed payment: {}", paymentIntentId);
        
        // Find associated payment in our system
        // Logic to update payment status, notify user, etc.
    }
} 