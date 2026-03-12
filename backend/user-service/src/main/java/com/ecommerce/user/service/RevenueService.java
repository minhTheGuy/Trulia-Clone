package com.ecommerce.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.user.payload.dto.RevenueStatisticsDTO;
import com.ecommerce.user.payload.dto.RevenueBySourceDTO;
import com.ecommerce.user.payload.dto.TransactionDTO;

public interface RevenueService {
    RevenueStatisticsDTO getRevenueStatistics(String timeRange, Integer year, Integer month, Integer quarter);
    RevenueBySourceDTO getRevenueBySource(String timeRange, Integer year, Integer month, Integer quarter);
    Page<TransactionDTO> getTransactions(Pageable pageable);
} 