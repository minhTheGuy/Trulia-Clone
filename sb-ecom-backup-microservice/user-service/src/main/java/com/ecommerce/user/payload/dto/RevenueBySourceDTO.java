package com.ecommerce.user.payload.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class RevenueBySourceDTO {
    private List<SourceData> data;

    @Data
    public static class SourceData {
        private String source;
        private BigDecimal amount;
    }
} 