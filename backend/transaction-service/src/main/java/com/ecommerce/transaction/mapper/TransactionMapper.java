package com.ecommerce.transaction.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.transaction.payload.dto.TransactionDTO;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.TransactionStatus;

@Component
public class TransactionMapper {

    public TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = TransactionDTO.builder()
                .id(transaction.getId())
                .propertyId(transaction.getPropertyId())
                .buyerId(transaction.getBuyerId())
                .sellerId(transaction.getSellerId())
                .amount(transaction.getAmount())
                .commission(transaction.getCommission())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .updatedAt(transaction.getUpdatedAt())
                .paymentMethod(transaction.getPaymentMethod())
                .contractNumber(transaction.getContractNumber())
                .propertyTitle(transaction.getPropertyTitle())
                .propertyAddress(transaction.getPropertyAddress())
                .buyerName(transaction.getBuyerName())
                .sellerName(transaction.getSellerName())
                .notes(transaction.getNotes())
                .depositPaid(transaction.isDepositPaid())
                .contractSigned(transaction.isContractSigned())
                .fullPaymentCompleted(transaction.isFullPaymentCompleted())
                .rentalPeriod(transaction.getRentalPeriod())
                .rentalStartDate(transaction.getRentalStartDate())
                .rentalEndDate(transaction.getRentalEndDate())
                .build();
        
        // Add additional information for the response
        dto.setStatusMessage(getStatusMessage(transaction.getStatus()));
        dto.setCanBeCancelled(canBeCancelled(transaction));
        
        return dto;
    }
    
    public Transaction mapToEntity(TransactionDTO dto) {
        return Transaction.builder()
                .id(dto.getId())
                .propertyId(dto.getPropertyId())
                .buyerId(dto.getBuyerId())
                .sellerId(dto.getSellerId())
                .amount(dto.getAmount())
                .commission(dto.getCommission())
                .transactionType(dto.getTransactionType())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .completedAt(dto.getCompletedAt())
                .updatedAt(dto.getUpdatedAt())
                .paymentMethod(dto.getPaymentMethod())
                .contractNumber(dto.getContractNumber())
                .propertyTitle(dto.getPropertyTitle())
                .propertyAddress(dto.getPropertyAddress())
                .buyerName(dto.getBuyerName())
                .sellerName(dto.getSellerName())
                .notes(dto.getNotes())
                .depositPaid(dto.isDepositPaid())
                .contractSigned(dto.isContractSigned())
                .fullPaymentCompleted(dto.isFullPaymentCompleted())
                .rentalPeriod(dto.getRentalPeriod())
                .rentalStartDate(dto.getRentalStartDate())
                .rentalEndDate(dto.getRentalEndDate())
                .build();
    }
    
    private String getStatusMessage(TransactionStatus status) {
        if (status == null) {
            return "Unknown";
        }
        
        return switch (status) {
            case PENDING -> "Transaction is pending";
            case DEPOSIT_PAID -> "Deposit has been paid";
            case CONTRACT_SIGNED -> "Contract has been signed";
            case IN_PROGRESS -> "Transaction is in progress";
            case COMPLETED -> "Transaction completed successfully";
            case CANCELLED -> "Transaction was cancelled";
            case FAILED -> "Transaction failed";
            case REFUNDED -> "Transaction was refunded";
            default -> "Unknown status";
        };
    }
    
    private boolean canBeCancelled(Transaction transaction) {
        // A transaction can be cancelled if it's not already completed, cancelled, failed or refunded
        return transaction.getStatus() != TransactionStatus.COMPLETED &&
               transaction.getStatus() != TransactionStatus.CANCELLED &&
               transaction.getStatus() != TransactionStatus.FAILED &&
               transaction.getStatus() != TransactionStatus.REFUNDED;
    }
} 