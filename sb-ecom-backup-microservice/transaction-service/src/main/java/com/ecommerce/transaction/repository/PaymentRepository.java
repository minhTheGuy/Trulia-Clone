package com.ecommerce.transaction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.transaction.model.Payment;
import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Find by transaction ID
    List<Payment> findByTransactionId(Long transactionId);
    
    // Find paginated by transaction ID
    Page<Payment> findByTransactionId(Long transactionId, Pageable pageable);
    
    // Find by payment status
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    // Find by payment method
    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    
    // Find by date range
    Page<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Find by Stripe payment intent ID
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    // Find by reference number
    Optional<Payment> findByReferenceNumber(String referenceNumber);
    
    // Count by status
    long countByStatus(PaymentStatus status);
    
    // Advanced search
    @Query("SELECT p FROM Payment p WHERE " +
           "(:transactionId IS NULL OR p.transactionId = :transactionId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND " +
           "(:startDate IS NULL OR p.paymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.paymentDate <= :endDate)")
    Page<Payment> searchPayments(
            @Param("transactionId") Long transactionId,
            @Param("status") PaymentStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
} 