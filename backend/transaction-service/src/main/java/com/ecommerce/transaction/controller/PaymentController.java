package com.ecommerce.transaction.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.PaymentStatus;
import com.ecommerce.transaction.payload.dto.PaymentDTO;
import com.ecommerce.transaction.payload.request.PaymentRequest;
import com.ecommerce.transaction.payload.request.RentalPaymentRequest;
import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Creating new payment for transaction ID: {}", request.getTransactionId());
        PaymentDTO payment = paymentService.createPayment(request);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        log.info("Fetching payment with ID: {}", id);
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping
    public ResponseEntity<PagedResponse<PaymentDTO>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long transactionId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching all payments with filters");
        PagedResponse<PaymentDTO> payments = paymentService.getAllPayments(
                page, size, sortBy, sortDir, transactionId, status, paymentMethod, startDate, endDate);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/rental-payment")
    public ResponseEntity<PaymentDTO> processRentalPayment(@Valid @RequestBody RentalPaymentRequest request) {
        log.info("Processing rental payment for transaction ID: {}", request.getTransactionId());
        
        // Convert rental payment request to standard payment request
        PaymentRequest paymentRequest = request.toPaymentRequest();
        
        // Process payment based on payment method
        PaymentDTO payment;
        if (request.getPaymentMethod() == PaymentMethod.STRIPE) {
            payment = paymentService.processStripePayment(paymentRequest);
        } else {
            payment = paymentService.processPaymentForTransaction(request.getTransactionId(), paymentRequest);
        }
        
        return ResponseEntity.ok(payment);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentDTO> updatePaymentStatus(
            @PathVariable Long id, 
            @RequestParam PaymentStatus status) {
        
        log.info("Updating status of payment ID: {} to {}", id, status);
        PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(updatedPayment);
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PagedResponse<PaymentDTO>> getPaymentsByTransactionId(
            @PathVariable Long transactionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching payments for transaction ID: {}", transactionId);
        PagedResponse<PaymentDTO> payments = paymentService.getPaymentsByTransactionId(transactionId, page, size);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentDTO> refundPayment(@PathVariable Long id) {
        log.info("Refunding payment with ID: {}", id);
        PaymentDTO refundedPayment = paymentService.refundPayment(id);
        return ResponseEntity.ok(refundedPayment);
    }
} 