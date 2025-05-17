package com.ecommerce.auth.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ecommerce.auth.model.PasswordResetToken;

@Repository
public class PasswordResetTokenRepository {
    private final Map<String, PasswordResetToken> tokens = new HashMap<>();

    public PasswordResetToken save(PasswordResetToken token) {
        tokens.put(token.getToken(), token);
        return token;
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void delete(PasswordResetToken token) {
        tokens.remove(token.getToken());
    }
} 