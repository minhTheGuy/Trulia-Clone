package com.ecommerce.user.payload.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class RevenueStatisticsDTO {
    private BigDecimal totalRevenue;
    private Integer totalTransactions;
    private BigDecimal averageTransactionValue;
    private Double percentageChange;
    private Integer pendingTransactions;
    private BigDecimal pendingAmount;
    private List<TimeSeriesDataPoint> timeSeriesData;

    @Data
    public static class TimeSeriesDataPoint {
        private String date;
        private BigDecimal revenue;
    }
} 