package com.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecommerce.auth.model.VerificationToken;
import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.repository.VerificationTokenRepository;
import com.ecommerce.auth.security.services.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final AuthUtil authUtil;

    @Value("${app.email.verification.expiration}")
    private long verificationTokenExpirationMs;

    public void createVerificationTokenAndSendEmail(UserDto userDto) {
        logger.info("Creating verification token for user: {}", userDto.getUsername());
        
        VerificationToken verificationToken = new VerificationToken(
            userDto.getId(),
            userDto.getUsername(),
            userDto.getEmail(),
            verificationTokenExpirationMs
        );
        
        verificationTokenRepository.save(verificationToken);
        
        // Send verification email
        emailService.sendVerificationEmail(
            userDto.getEmail(),
            userDto.getUsername(),
            verificationToken.getToken()
        );
        
        logger.info("Verification token created and email scheduled: {}", verificationToken.getToken());
    }

    public boolean verifyAccount(String token) {
        logger.info("Verifying token: {}", token);

        return verificationTokenRepository.findByToken(token)
            .filter(verificationToken -> !verificationToken.isUsed() && !verificationToken.isExpired())
            .map(verificationToken -> {
                // Mark token as used
                verificationToken.setUsed(true);
                verificationTokenRepository.save(verificationToken);

                // Activate user account
                boolean activated = authUtil.activateUserAccount(verificationToken.getUserId());
                logger.info("Account activation result for user {}: {}",
                          verificationToken.getUsername(), activated ? "success" : "failed");
                return activated;
            })
            .orElseGet(() -> {
                logger.warn("Invalid or expired token: {}", token);
                return false;
            });
    }
}