package com.ecommerce.transaction.mapper;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ecommerce.transaction.payload.dto.PaymentDTO;
import com.ecommerce.transaction.model.Payment;
import com.ecommerce.transaction.model.PaymentStatus;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.repository.TransactionRepository;

@Component
public class PaymentMapper {

    @Autowired
    private TransactionRepository transactionRepository;
    
    public PaymentDTO mapToDTO(Payment payment) {
        PaymentDTO dto = PaymentDTO.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeCustomerId(payment.getStripeCustomerId())
                .referenceNumber(payment.getReferenceNumber())
                .description(payment.getDescription())
                .receiptUrl(payment.getReceiptUrl())
                .build();
        
        // Add additional information for the response
        dto.setStatusMessage(getStatusMessage(payment.getStatus()));
        
        // If transaction ID is provided, fetch transaction details
        if (payment.getTransactionId() != null) {
            Optional<Transaction> transactionOpt = transactionRepository.findById(payment.getTransactionId());
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                dto.setTransactionType(transaction.getTransactionType().name());
                dto.setPropertyTitle(transaction.getPropertyTitle());
            }
        }
        
        return dto;
    }
    
    public Payment mapToEntity(PaymentDTO dto) {
        return Payment.builder()
                .id(dto.getId())
                .transactionId(dto.getTransactionId())
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .status(dto.getStatus())
                .paymentDate(dto.getPaymentDate())
                .stripePaymentIntentId(dto.getStripePaymentIntentId())
                .stripeCustomerId(dto.getStripeCustomerId())
                .referenceNumber(dto.getReferenceNumber())
                .description(dto.getDescription())
                .receiptUrl(dto.getReceiptUrl())
                .build();
    }
    
    private String getStatusMessage(PaymentStatus status) {
        if (status == null) {
            return "Unknown";
        }
        
        return switch (status) {
            case PENDING -> "Payment is pending";
            case PROCESSING -> "Payment is being processed";
            case COMPLETED -> "Payment is completed";
            case FAILED -> "Payment failed";
            case REFUNDED -> "Payment was refunded";
            case CANCELLED -> "Payment was cancelled";
            default -> "Unknown status";
        };
    }
} 