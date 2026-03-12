package com.ecommerce.transaction.payload.dto;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Map;

import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatsDTO {
    // Status counts
    private Map<TransactionStatus, Long> statusCounts;
    
    // Type counts
    private Map<TransactionType, Long> typeCounts;
    
    // Total amount
    private BigDecimal totalAmount;
    
    // Completed amount
    private BigDecimal completedAmount;
    
    // Monthly stats
    private Map<Month, BigDecimal> monthlyStats;
} 