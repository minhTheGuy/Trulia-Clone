package com.ecommerce.transaction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.transaction.payload.request.RentalPaymentRequest;
import com.ecommerce.transaction.payload.response.StripeResponse;
import com.ecommerce.transaction.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stripe")
public class StripeSessionController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-rental-checkout-session")
    public ResponseEntity<StripeResponse> createRentalCheckoutSession(
            @Valid @RequestBody RentalPaymentRequest request) throws StripeException {

        Session session = stripeService.createRentalCheckoutSession(request);

        StripeResponse response = new StripeResponse(
            session.getId(),
            session.getUrl()
        );

        return ResponseEntity.ok(response);
    }
} 