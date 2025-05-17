package com.ecommerce.transaction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long propertyId;
    private Long buyerId;
    private Long sellerId;
    
    @Column(precision = 20, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 20, scale = 2)
    private BigDecimal commission;
    
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private String contractNumber;
    
    // Property information
    private String propertyTitle;
    private String propertyAddress;
    
    // User information
    private String buyerName;
    private String sellerName;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Transaction progress tracking
    private boolean depositPaid;
    private boolean contractSigned;
    private boolean fullPaymentCompleted;
    
    // For rental transactions
    private String rentalPeriod;
    private LocalDateTime rentalStartDate;
    private LocalDateTime rentalEndDate;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        
        // If all steps are completed and transaction is in progress, mark as completed
        if (depositPaid && contractSigned && fullPaymentCompleted && 
            status == TransactionStatus.IN_PROGRESS) {
            status = TransactionStatus.COMPLETED;
            completedAt = LocalDateTime.now();
        }
    }
} 