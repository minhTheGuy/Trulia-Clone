package com.ecommerce.transaction.service;

import java.time.LocalDateTime;

import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.payload.dto.PaymentDTO;
import com.ecommerce.transaction.payload.request.PaymentRequest;
import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.PaymentStatus;

public interface PaymentService {
    // Create a new payment
    PaymentDTO createPayment(PaymentRequest request);
    
    // Get a payment by ID
    PaymentDTO getPaymentById(Long id);
    
    // Get paginated payments with filtering
    PagedResponse<PaymentDTO> getAllPayments(
            int page, int size, String sortBy, String sortDir,
            Long transactionId, PaymentStatus status, PaymentMethod paymentMethod,
            LocalDateTime startDate, LocalDateTime endDate);
    
    // Process Stripe payment
    PaymentDTO processStripePayment(PaymentRequest request);
    
    // Process payment for a transaction
    PaymentDTO processPaymentForTransaction(Long transactionId, PaymentRequest request);
    
    // Update payment status
    PaymentDTO updatePaymentStatus(Long id, PaymentStatus newStatus);
    
    // Get payments by transaction ID
    PagedResponse<PaymentDTO> getPaymentsByTransactionId(Long transactionId, int page, int size);
    
    // Refund a payment
    PaymentDTO refundPayment(Long id);
} 