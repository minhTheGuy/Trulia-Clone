package com.ecommerce.user.payload.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionDTO {
    private Long id;
    private String userName;
    private String type;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
} 