package com.ecommerce.auth.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ecommerce.auth.model.VerificationToken;

@Repository
public class VerificationTokenRepository {
    private final Map<String, VerificationToken> tokens = new HashMap<>();

    public VerificationToken save(VerificationToken token) {
        tokens.put(token.getToken(), token);
        return token;
    }

    public Optional<VerificationToken> findByToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void delete(VerificationToken token) {
        tokens.remove(token.getToken());
    }
} 