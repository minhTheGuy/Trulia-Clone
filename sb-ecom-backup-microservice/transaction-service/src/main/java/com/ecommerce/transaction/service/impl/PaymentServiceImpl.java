package com.ecommerce.transaction.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.payload.dto.PaymentDTO;
import com.ecommerce.transaction.payload.request.PaymentRequest;
import com.ecommerce.transaction.exception.ResourceNotFoundException;
import com.ecommerce.transaction.exception.TransactionApiException;
import com.ecommerce.transaction.mapper.PaymentMapper;
import com.ecommerce.transaction.model.Payment;
import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.PaymentStatus;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;
import com.ecommerce.transaction.repository.PaymentRepository;
import com.ecommerce.transaction.repository.TransactionRepository;
import com.ecommerce.transaction.service.NotificationService;
import com.ecommerce.transaction.service.PaymentService;
import com.ecommerce.transaction.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private StripeService stripeService;

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentRequest request) {
        log.info("Creating payment for transaction ID: {}", request.getTransactionId());
        
        // Validate transaction exists
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", request.getTransactionId()));
        
        // Create payment entity
        Payment payment = Payment.builder()
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .referenceNumber(request.getReferenceNumber())
                .description(request.getDescription())
                .build();
        
        // Set payment date to current time
        payment.prePersist();
        
        payment = paymentRepository.save(payment);
        
        // Send notification
        notificationService.sendPaymentReceivedNotification(
                request.getTransactionId(), 
                request.getAmount().toString());
        
        return paymentMapper.mapToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentById(Long id) {
        log.info("Getting payment with ID: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        return paymentMapper.mapToDTO(payment);
    }

    @Override
    public PagedResponse<PaymentDTO> getAllPayments(
            int page, int size, String sortBy, String sortDir,
            Long transactionId, PaymentStatus status, PaymentMethod paymentMethod,
            LocalDateTime startDate, LocalDateTime endDate) {
        
        log.info("Getting all payments with filters");
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Payment> payments = paymentRepository.searchPayments(
                transactionId, status, paymentMethod, startDate, endDate, pageable);
        
        List<PaymentDTO> content = payments.getContent().stream()
                .map(paymentMapper::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content, 
                payments.getNumber(), 
                payments.getSize(), 
                payments.getTotalElements(), 
                payments.getTotalPages(), 
                payments.isLast());
    }

    @Override
    @Transactional
    public PaymentDTO processStripePayment(PaymentRequest request) {
        log.info("Processing Stripe payment for transaction ID: {}", request.getTransactionId());
        
        // Validate transaction exists
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", request.getTransactionId()));
        
        // Create a pending payment record
        final Payment payment = Payment.builder()
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .paymentMethod(PaymentMethod.STRIPE)
                .status(PaymentStatus.PROCESSING)
                .description(request.getDescription())
                .build();
        
        payment.prePersist();
        final Payment savedPayment = paymentRepository.save(payment);
        
        try {
            // Create metadata for Stripe
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("transaction_id", transaction.getId());
            metadata.put("payment_id", savedPayment.getId());
            metadata.put("property_id", transaction.getPropertyId());
            
            PaymentDTO paymentDTO;
            
            // Nếu có payment_method_id từ client (từ frontend)
            if (request.getStripeToken() != null && !request.getStripeToken().isEmpty()) {
                log.info("Using payment method ID from client: {}", request.getStripeToken());
                
                // Xử lý thanh toán trực tiếp với payment method ID
                String paymentMethodId = request.getStripeToken();
                
                // Giả lập thanh toán thành công cho môi trường test
                // Trong môi trường production, bạn sẽ thực sự gọi API Stripe ở đây
                Payment confirmedPayment = savedPayment;
                confirmedPayment.setStatus(PaymentStatus.COMPLETED);
                confirmedPayment.setStripePaymentIntentId("pi_" + System.currentTimeMillis());
                confirmedPayment.setStripeCustomerId(request.getStripeCustomerId());
                confirmedPayment = paymentRepository.save(confirmedPayment);
                
                // Nếu thành công, cập nhật trạng thái giao dịch
                if (transaction != null) {
                    updateTransactionStatusOnPayment(transaction, confirmedPayment);
                }
                
                paymentDTO = paymentMapper.mapToDTO(confirmedPayment);
                paymentDTO.setClientSecret("test_client_secret");
            } else {
                // Tạo payment intent như cũ nếu không có payment_method_id
                PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                        request.getAmount(), "usd", metadata);
                
                // Get the client secret from the payment intent - this is crucial for the frontend
                String clientSecret = paymentIntent.getClientSecret();
                
                // Update payment with Stripe payment intent ID
                Payment updatedPayment = savedPayment;
                updatedPayment.setStripePaymentIntentId(paymentIntent.getId());
                updatedPayment.setStripeCustomerId(request.getStripeCustomerId());
                updatedPayment = paymentRepository.save(updatedPayment);
                
                // Create DTO with all necessary information including client secret
                paymentDTO = paymentMapper.mapToDTO(updatedPayment);
                paymentDTO.setClientSecret(clientSecret);
                
                log.info("Payment intent created with ID: {}, returning client secret to frontend", paymentIntent.getId());
            }
            
            return paymentDTO;
            
        } catch (Exception e) {
            log.error("Error processing Stripe payment", e);
            Payment failedPayment = savedPayment;
            failedPayment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(failedPayment);
            throw new TransactionApiException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error processing payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentDTO processPaymentForTransaction(Long transactionId, PaymentRequest request) {
        log.info("Processing payment for transaction ID: {}", transactionId);
        
        // Validate transaction exists
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        
        // Create payment record
        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.COMPLETED) // Manual payments are marked as completed
                .referenceNumber(request.getReferenceNumber())
                .description(request.getDescription())
                .build();
        
        payment.prePersist();
        payment = paymentRepository.save(payment);
        
        // Update transaction status if needed
        updateTransactionStatusOnPayment(transaction, payment);
        
        // Send notification
        notificationService.sendPaymentReceivedNotification(
                transactionId, 
                request.getAmount().toString());
        
        return paymentMapper.mapToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Long id, PaymentStatus newStatus) {
        log.info("Updating status of payment ID: {} to {}", id, newStatus);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        payment.setStatus(newStatus);
        final Payment updatedPayment = paymentRepository.save(payment);
        
        // If payment is completed, update transaction
        if (newStatus == PaymentStatus.COMPLETED) {
            Transaction transaction = transactionRepository.findById(payment.getTransactionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", payment.getTransactionId()));
            
            updateTransactionStatusOnPayment(transaction, payment);
        }
        
        return paymentMapper.mapToDTO(updatedPayment);
    }

    @Override
    public PagedResponse<PaymentDTO> getPaymentsByTransactionId(Long transactionId, int page, int size) {
        log.info("Getting payments for transaction ID: {}", transactionId);
        
        // Validate transaction exists
        transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        
        Page<Payment> payments = paymentRepository.findByTransactionId(transactionId, pageable);
        
        List<PaymentDTO> content = payments.getContent().stream()
                .map(paymentMapper::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content, 
                payments.getNumber(), 
                payments.getSize(), 
                payments.getTotalElements(), 
                payments.getTotalPages(), 
                payments.isLast());
    }

    @Override
    @Transactional
    public PaymentDTO refundPayment(Long id) {
        log.info("Refunding payment with ID: {}", id);
        
        final Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        
        // Ensure payment is completed
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new TransactionApiException(HttpStatus.BAD_REQUEST, 
                    "Cannot refund payment with status: " + payment.getStatus());
        }
        
        // For Stripe payments, process through Stripe API
        if (payment.getPaymentMethod() == PaymentMethod.STRIPE && payment.getStripePaymentIntentId() != null) {
            try {
                stripeService.createRefund(payment.getStripePaymentIntentId(), payment.getAmount());
            } catch (StripeException e) {
                log.error("Error processing Stripe refund", e);
                throw new TransactionApiException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "Error processing refund: " + e.getMessage());
            }
        }
        
        // Update payment status
        Payment updatedPayment = paymentRepository.findById(id).get();
        updatedPayment.setStatus(PaymentStatus.REFUNDED);
        updatedPayment = paymentRepository.save(updatedPayment);
        
        // Update transaction status if needed
        Transaction transaction = transactionRepository.findById(payment.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", payment.getTransactionId()));
        
        transaction.setStatus(TransactionStatus.REFUNDED);
        transactionRepository.save(transaction);
        
        return paymentMapper.mapToDTO(updatedPayment);
    }
    
    // Helper method to update transaction status based on payment
    private void updateTransactionStatusOnPayment(Transaction transaction, Payment payment) {
        // Update transaction status based on payment
        if (transaction.getTransactionType().name().contains("RENT") || 
            transaction.getTransactionType() == TransactionType.DEPOSIT) {
            
            transaction.setDepositPaid(true);
            transaction.setStatus(TransactionStatus.DEPOSIT_PAID);
            
        } else if (transaction.getTransactionType() == TransactionType.SALE) {
            // For sale transactions, mark as in progress after initial payment
            transaction.setDepositPaid(true);
            
            // If this is a full payment, mark transaction as completed
            if (payment.getAmount().compareTo(transaction.getAmount()) >= 0) {
                transaction.setFullPaymentCompleted(true);
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
            } else {
                transaction.setStatus(TransactionStatus.IN_PROGRESS);
            }
        }
        
        transactionRepository.save(transaction);
    }
} 