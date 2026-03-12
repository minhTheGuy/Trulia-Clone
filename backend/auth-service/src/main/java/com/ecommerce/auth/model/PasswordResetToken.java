package com.ecommerce.auth.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordResetToken {
    private Long userId;
    private String username;
    private String email;
    private String token;
    private Instant expiryDate;
    private boolean used = false;
    
    public PasswordResetToken(Long userId, String username, String email, long expirationMs) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = Instant.now().plusMillis(expirationMs);
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
} 