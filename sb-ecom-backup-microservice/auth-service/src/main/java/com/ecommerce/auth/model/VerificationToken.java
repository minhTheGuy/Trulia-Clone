package com.ecommerce.auth.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerificationToken {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private Instant expiryDate;
    private boolean used;

    public VerificationToken(Long userId, String username, String email, long expirationMs) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.expiryDate = Instant.now().plusMillis(expirationMs);
        this.used = false;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
} 