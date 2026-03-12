package com.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.auth.model.PasswordResetToken;
import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.payload.request.ResetPasswordRequest;
import com.ecommerce.auth.repository.PasswordResetTokenRepository;
import com.ecommerce.auth.security.services.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final AuthUtil authUtil;

    @Value("${app.email.reset-password.expiration:3600000}") // Default 1 hour
    private long passwordResetTokenExpirationMs;

    /**
     * Creates a password reset token and sends an email with reset link
     * @param email User's email address
     * @return true if request was processed, false if email doesn't exist
     */
    public boolean initiatePasswordReset(String email) {
        logger.info("Initiating password reset for email: {}", email);
        
        // Find user by email
        try {
            UserDto userDto = authUtil.getUserByEmail(email);
            if (userDto == null) {
                logger.warn("No user found with email: {}", email);
                return false;
            }
            
            // Create reset token
            PasswordResetToken resetToken = new PasswordResetToken(
                userDto.getId(),
                userDto.getUsername(),
                userDto.getEmail(),
                passwordResetTokenExpirationMs
            );
            
            passwordResetTokenRepository.save(resetToken);
            
            // Send reset email
            emailService.sendPasswordResetEmail(
                userDto.getEmail(),
                userDto.getUsername(),
                resetToken.getToken()
            );
            
            logger.info("Password reset token created and email sent for user: {}", userDto.getUsername());
            return true;
        } catch (Exception e) {
            logger.error("Error creating password reset token: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Validates if a reset token is valid (exists and not expired)
     * @param token The password reset token
     * @return The associated user if token is valid
     */
    public UserDto validatePasswordResetToken(String token) {
        logger.info("Validating password reset token: {}", token);
        
        return passwordResetTokenRepository.findByToken(token)
            .filter(resetToken -> !resetToken.isUsed() && !resetToken.isExpired())
            .map(resetToken -> {
                try {
                    return authUtil.getUserById(resetToken.getUserId());
                } catch (Exception e) {
                    logger.error("Error fetching user for token: {}", e.getMessage(), e);
                    return null;
                }
            })
            .orElseGet(() -> {
                logger.warn("Invalid or expired password reset token: {}", token);
                return null;
            });
    }
    
    /**
     * Resets a user's password using a valid token
     * @param resetRequest The reset request containing token and new password
     * @return true if password was reset successfully
     */
    public boolean resetPassword(ResetPasswordRequest resetRequest) {
        logger.info("Processing password reset for token: {}", resetRequest.getToken());
        
        return passwordResetTokenRepository.findByToken(resetRequest.getToken())
            .filter(resetToken -> !resetToken.isUsed() && !resetToken.isExpired())
            .map(resetToken -> {
                try {
                    // Mark token as used to prevent reuse
                    resetToken.setUsed(true);
                    passwordResetTokenRepository.save(resetToken);
                    
                    // Update user's password
                    boolean updated = authUtil.updateUserPassword(
                        resetToken.getUserId(), 
                        resetRequest.getNewPassword()
                    );
                    
                    if (updated) {
                        logger.info("Password reset successful for user: {}", resetToken.getUsername());
                    } else {
                        logger.error("Password reset failed for user: {}", resetToken.getUsername());
                    }
                    
                    return updated;
                } catch (Exception e) {
                    logger.error("Error during password reset: {}", e.getMessage(), e);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                            "Password reset failed: " + e.getMessage());
                }
            })
            .orElseThrow(() -> {
                logger.warn("Invalid or expired password reset token: {}", resetRequest.getToken());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid or expired token. Please request a new password reset link.");
            });
    }
} 