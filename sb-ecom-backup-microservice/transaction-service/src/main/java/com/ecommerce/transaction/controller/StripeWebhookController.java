package com.ecommerce.transaction.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.transaction.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/webhook")
public class StripeWebhookController {
    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);
    
    @Autowired
    private StripeService stripeService;
    
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received webhook from Stripe");
        
        try {
            stripeService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Webhook signature verification failed: " + e.getMessage());
        } catch (StripeException e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Webhook Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }
} 