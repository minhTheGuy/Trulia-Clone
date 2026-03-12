package com.ecommerce.transaction.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find by property ID
    Page<Transaction> findByPropertyId(Long propertyId, Pageable pageable);
    
    // Find by buyer ID
    Page<Transaction> findByBuyerId(Long buyerId, Pageable pageable);
    
    // Find by seller ID
    Page<Transaction> findBySellerId(Long sellerId, Pageable pageable);
    
    // Find by status
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    
    // Find by type
    Page<Transaction> findByTransactionType(TransactionType type, Pageable pageable);
    
    // Find by date range
    Page<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Count by status
    long countByStatus(TransactionStatus status);
    
    // Count by type
    long countByTransactionType(TransactionType type);
    
    // Sum total amount of all transactions
    @Query("SELECT SUM(t.amount) FROM Transaction t")
    BigDecimal sumTotalAmount();
    
    // Sum amount of completed transactions
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal sumCompletedAmount();
    
    // Sum amount by year and month
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE FUNCTION('YEAR', t.createdAt) = :year AND FUNCTION('MONTH', t.createdAt) = :month")
    BigDecimal sumAmountByYearAndMonth(@Param("year") int year, @Param("month") int month);
    
    // Monthly transactions 
    @Query("SELECT FUNCTION('YEAR', t.createdAt) as year, FUNCTION('MONTH', t.createdAt) as month, COUNT(t) as count " +
           "FROM Transaction t " +
           "WHERE t.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', t.createdAt), FUNCTION('MONTH', t.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyTransactionCount(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    // Monthly transaction amounts
    @Query("SELECT FUNCTION('YEAR', t.createdAt) as year, FUNCTION('MONTH', t.createdAt) as month, SUM(t.amount) as total " +
           "FROM Transaction t " +
           "WHERE t.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', t.createdAt), FUNCTION('MONTH', t.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyTransactionAmount(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    // Advanced search
    @Query(value = """
            SELECT t FROM Transaction t 
            WHERE (:propertyId IS NULL OR t.propertyId = :propertyId) 
            AND (:buyerId IS NULL OR t.buyerId = :buyerId) 
            AND (:sellerId IS NULL OR t.sellerId = :sellerId) 
            AND (:status IS NULL OR t.status = :status) 
            AND (:type IS NULL OR t.transactionType = :type) 
            AND (CAST(:startDate AS timestamp) IS NULL OR t.createdAt >= :startDate) 
            AND (CAST(:endDate AS timestamp) IS NULL OR t.createdAt <= :endDate)
            """)
    Page<Transaction> searchTransactions(
            @Param("propertyId") Long propertyId,
            @Param("buyerId") Long buyerId,
            @Param("sellerId") Long sellerId,
            @Param("status") TransactionStatus status,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);

    long countByStatusAndCreatedAtBetween(TransactionStatus status, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}