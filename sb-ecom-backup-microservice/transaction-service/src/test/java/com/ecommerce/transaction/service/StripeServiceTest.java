package com.ecommerce.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.transaction.service.impl.StripeServiceImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@ExtendWith(MockitoExtension.class)
public class StripeServiceTest {

    @InjectMocks
    private StripeServiceImpl stripeService;

    @Mock
    private PaymentIntent mockPaymentIntent;

    private BigDecimal amount;
    private String currency;
    private Map<String, Object> metadata;

    @BeforeEach
    void setUp() {
        amount = new BigDecimal("1000000");
        currency = "VND";
        metadata = new HashMap<>();
        metadata.put("propertyId", "1");
        metadata.put("transactionType", "RENT");
    }

    @Test
    void createPaymentIntent_Success() throws StripeException {
        // Arrange
        when(mockPaymentIntent.create(any(PaymentIntentCreateParams.class))).thenReturn(mockPaymentIntent);
        when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
        when(mockPaymentIntent.getClientSecret()).thenReturn("secret_test_123");

        // Act
        PaymentIntent result = stripeService.createPaymentIntent(amount, currency, metadata);

        // Assert
        assertNotNull(result);
        assertEquals("pi_test_123", result.getId());
        assertEquals("secret_test_123", result.getClientSecret());
    }

    @Test
    void confirmPaymentIntent_Success() throws StripeException {
        // Arrange
        String paymentIntentId = "pi_test_123";
        when(mockPaymentIntent.retrieve(paymentIntentId)).thenReturn(mockPaymentIntent);
        when(mockPaymentIntent.confirm()).thenReturn(mockPaymentIntent);
        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");

        // Act
        PaymentIntent result = stripeService.confirmPaymentIntent(paymentIntentId);

        // Assert
        assertNotNull(result);
        assertEquals("succeeded", result.getStatus());
    }

    @Test
    void cancelPaymentIntent_Success() throws StripeException {
        // Arrange
        String paymentIntentId = "pi_test_123";
        when(mockPaymentIntent.retrieve(paymentIntentId)).thenReturn(mockPaymentIntent);
        when(mockPaymentIntent.cancel()).thenReturn(mockPaymentIntent);
        when(mockPaymentIntent.getStatus()).thenReturn("canceled");

        // Act
        PaymentIntent result = stripeService.cancelPaymentIntent(paymentIntentId);

        // Assert
        assertNotNull(result);
        assertEquals("canceled", result.getStatus());
    }
} 