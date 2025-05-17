package com.ecommerce.transaction.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeResponse {
    private String sessionId;
    private String sessionUrl;
} 