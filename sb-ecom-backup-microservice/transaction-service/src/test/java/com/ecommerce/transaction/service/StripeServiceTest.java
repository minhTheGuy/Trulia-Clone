package com.ecommerce.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.transaction.service.impl.StripeServiceImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@ExtendWith(MockitoExtension.class)
public class StripeServiceTest {

    @Mock
    private PaymentIntent mockPaymentIntent;

    private StripeServiceImpl stripeService;

    private BigDecimal amount;
    private String currency;
    private Map<String, Object> metadata;

    @BeforeEach
    void setUp() {
        // Manually create the service with a test API key
        stripeService = new StripeServiceImpl("sk_test_mockApiKey");

        amount = new BigDecimal("1000000");
        currency = "VND";
        metadata = new HashMap<>();
        metadata.put("propertyId", "1");
        metadata.put("transactionType", "RENT");
    }

    @Test
    void createPaymentIntent_Success() throws StripeException {
        // Use MockedStatic to mock Stripe's static PaymentIntent.create method
        try (MockedStatic<PaymentIntent> paymentIntentMockedStatic = mockStatic(PaymentIntent.class)) {
            // Arrange
            paymentIntentMockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class))).thenReturn(mockPaymentIntent);
            paymentIntentMockedStatic.when(() -> PaymentIntent.retrieve(anyString())).thenReturn(mockPaymentIntent);

            when(mockPaymentIntent.getId()).thenReturn("pi_test_123");
            when(mockPaymentIntent.getClientSecret()).thenReturn("secret_test_123");
            when(mockPaymentIntent.update(any(Map.class))).thenReturn(mockPaymentIntent);

            // Act
            PaymentIntent result = stripeService.createPaymentIntent(amount, currency, metadata);

            // Assert
            assertNotNull(result);
            assertEquals("pi_test_123", result.getId());
            assertEquals("secret_test_123", result.getClientSecret());

            // Verify that methods were called
            verify(mockPaymentIntent, atLeastOnce()).getId();
            verify(mockPaymentIntent).getClientSecret();
        }
    }

    @Test
    void confirmPaymentIntent_Success() throws StripeException {
        // Use MockedStatic to mock Stripe's static PaymentIntent.retrieve method
        try (MockedStatic<PaymentIntent> paymentIntentMockedStatic = mockStatic(PaymentIntent.class)) {
            // Arrange
            String paymentIntentId = "pi_test_123";
            paymentIntentMockedStatic.when(() -> PaymentIntent.retrieve(anyString())).thenReturn(mockPaymentIntent);
            when(mockPaymentIntent.confirm()).thenReturn(mockPaymentIntent);
            when(mockPaymentIntent.getStatus()).thenReturn("succeeded");

            // Act
            PaymentIntent result = stripeService.confirmPaymentIntent(paymentIntentId);

            // Assert
            assertNotNull(result);
            assertEquals("succeeded", result.getStatus());

            // Verify
            verify(mockPaymentIntent).confirm();
            verify(mockPaymentIntent).getStatus();
        }
    }

    @Test
    void cancelPaymentIntent_Success() throws StripeException {
        // Use MockedStatic to mock Stripe's static PaymentIntent.retrieve method
        try (MockedStatic<PaymentIntent> paymentIntentMockedStatic = mockStatic(PaymentIntent.class)) {
            // Arrange
            String paymentIntentId = "pi_test_123";
            paymentIntentMockedStatic.when(() -> PaymentIntent.retrieve(anyString())).thenReturn(mockPaymentIntent);
            when(mockPaymentIntent.cancel()).thenReturn(mockPaymentIntent);
            when(mockPaymentIntent.getStatus()).thenReturn("canceled");

            // Act
            PaymentIntent result = stripeService.cancelPaymentIntent(paymentIntentId);

            // Assert
            assertNotNull(result);
            assertEquals("canceled", result.getStatus());

            // Verify
            verify(mockPaymentIntent).cancel();
            verify(mockPaymentIntent).getStatus();
        }
    }
}

