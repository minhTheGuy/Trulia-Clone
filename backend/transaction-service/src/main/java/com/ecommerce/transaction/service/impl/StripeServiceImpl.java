package com.ecommerce.transaction.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.transaction.model.Payment;
import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.PaymentStatus;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;
import com.ecommerce.transaction.payload.request.RentalPaymentRequest;
import com.ecommerce.transaction.publisher.TransactionEventPublisher;
import com.ecommerce.transaction.repository.PaymentRepository;
import com.ecommerce.transaction.repository.TransactionRepository;
import com.ecommerce.transaction.service.StripeService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionEventPublisher eventPublisher;
    
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

        // VND is a zero-decimal currency — do not multiply by 100
        long amountInSmallestUnit = amount.longValue();

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amountInSmallestUnit)
                .build();

        Refund refund = Refund.create(params);

        log.info("Created refund: {} for payment intent: {}", refund.getId(), paymentIntentId);
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) throws StripeException {
        log.info("Handling Stripe webhook event");

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            throw new StripeException(e.getMessage(), null, null, null) {};
        }

        log.info("Verified webhook signature, event type: {}", event.getType());

        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;

                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
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

    private void handleCheckoutSessionCompleted(Event event) {
        String rawJson = event.getDataObjectDeserializer().getRawJson();
        JsonObject sessionJson = JsonParser.parseString(rawJson).getAsJsonObject();

        JsonObject metadata = sessionJson.getAsJsonObject("metadata");
        if (metadata == null || !metadata.has("transactionId")) {
            log.warn("checkout.session.completed event missing transactionId metadata");
            return;
        }

        Long transactionId = metadata.get("transactionId").getAsLong();
        String paymentIntentId = sessionJson.has("payment_intent") && !sessionJson.get("payment_intent").isJsonNull()
                ? sessionJson.get("payment_intent").getAsString() : null;
        long amountTotal = sessionJson.has("amount_total") ? sessionJson.get("amount_total").getAsLong() : 0L;

        log.info("Checkout session completed for transactionId={}, paymentIntentId={}", transactionId, paymentIntentId);

        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            log.error("Transaction not found for ID: {}", transactionId);
            return;
        }

        // Create or update the payment record
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .stream().findFirst().orElse(Payment.builder()
                        .transactionId(transactionId)
                        .paymentMethod(PaymentMethod.STRIPE)
                        .build());

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(BigDecimal.valueOf(amountTotal));
        payment.setStripePaymentIntentId(paymentIntentId);
        if (payment.getPaymentDate() == null) {
            payment.prePersist();
        }
        payment = paymentRepository.save(payment);

        // Update transaction status
        updateTransactionOnPaymentSuccess(transaction);

        // Publish Kafka event
        eventPublisher.publishPaymentCompleted(transaction, payment);
    }

    private void handlePaymentIntentSucceeded(Event event) {
        String rawJson = event.getDataObjectDeserializer().getRawJson();
        JsonObject piJson = JsonParser.parseString(rawJson).getAsJsonObject();
        String paymentIntentId = piJson.get("id").getAsString();

        log.info("PaymentIntent succeeded: {}", paymentIntentId);

        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            transactionRepository.findById(payment.getTransactionId()).ifPresent(transaction -> {
                updateTransactionOnPaymentSuccess(transaction);
                eventPublisher.publishPaymentCompleted(transaction, payment);
            });
        });
    }

    private void handlePaymentIntentFailed(Event event) {
        String rawJson = event.getDataObjectDeserializer().getRawJson();
        JsonObject piJson = JsonParser.parseString(rawJson).getAsJsonObject();
        String paymentIntentId = piJson.get("id").getAsString();

        log.warn("PaymentIntent failed: {}", paymentIntentId);

        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            transactionRepository.findById(payment.getTransactionId()).ifPresent(transaction -> {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                eventPublisher.publishPaymentFailed(transaction, payment);
            });
        });
    }

    private void updateTransactionOnPaymentSuccess(Transaction transaction) {
        if (transaction.getTransactionType() == TransactionType.RENT ||
                transaction.getTransactionType() == TransactionType.DEPOSIT) {
            transaction.setDepositPaid(true);
            transaction.setStatus(TransactionStatus.DEPOSIT_PAID);
        } else if (transaction.getTransactionType() == TransactionType.SALE) {
            transaction.setDepositPaid(true);
            transaction.setFullPaymentCompleted(true);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
        }
        transactionRepository.save(transaction);
    }
} 