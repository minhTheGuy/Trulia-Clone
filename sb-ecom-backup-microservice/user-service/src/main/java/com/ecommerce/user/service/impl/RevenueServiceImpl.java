package com.ecommerce.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.user.client.TransactionServiceClient;
import com.ecommerce.user.payload.dto.RevenueStatisticsDTO;
import com.ecommerce.user.payload.dto.RevenueBySourceDTO;
import com.ecommerce.user.payload.dto.TransactionDTO;
import com.ecommerce.user.service.RevenueService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final TransactionServiceClient transactionServiceClient;

    @Override
    public RevenueStatisticsDTO getRevenueStatistics(String timeRange, Integer year, Integer month, Integer quarter) {
        try {
            return transactionServiceClient.getRevenueStatistics(timeRange, year, month, quarter);
        } catch (FeignException e) {
            log.error("Error fetching revenue statistics from transaction-service", e);
            throw new RuntimeException("Failed to fetch revenue statistics", e);
        }
    }

    @Override
    public RevenueBySourceDTO getRevenueBySource(String timeRange, Integer year, Integer month, Integer quarter) {
        try {
            return transactionServiceClient.getRevenueBySource(timeRange, year, month, quarter);
        } catch (FeignException e) {
            log.error("Error fetching revenue by source from transaction-service", e);
            throw new RuntimeException("Failed to fetch revenue by source", e);
        }
    }

    @Override
    public Page<TransactionDTO> getTransactions(Pageable pageable) {
        try {
            String[] sort = {pageable.getSort().iterator().next().getProperty(),
                           pageable.getSort().iterator().next().getDirection().name().toLowerCase()};
            return transactionServiceClient.getTransactions(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
            );
        } catch (FeignException e) {
            log.error("Error fetching transactions from transaction-service", e);
            throw new RuntimeException("Failed to fetch transactions", e);
        }
    }
} 