package com.ecommerce.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.user.payload.dto.RevenueStatisticsDTO;
import com.ecommerce.user.payload.dto.RevenueBySourceDTO;
import com.ecommerce.user.payload.dto.TransactionDTO;

@FeignClient(name = "transaction-service")
public interface TransactionServiceClient {
    
    @GetMapping("/api/transactions/revenue/statistics")
    RevenueStatisticsDTO getRevenueStatistics(
        @RequestParam("timeRange") String timeRange,
        @RequestParam("year") Integer year,
        @RequestParam(value = "month", required = false) Integer month,
        @RequestParam(value = "quarter", required = false) Integer quarter
    );

    @GetMapping("/api/transactions/revenue/by-source")
    RevenueBySourceDTO getRevenueBySource(
        @RequestParam("timeRange") String timeRange,
        @RequestParam("year") Integer year,
        @RequestParam(value = "month", required = false) Integer month,
        @RequestParam(value = "quarter", required = false) Integer quarter
    );

    @GetMapping("/api/transactions")
    Page<TransactionDTO> getTransactions(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "sort", defaultValue = "createdAt,desc") String[] sort
    );
} 