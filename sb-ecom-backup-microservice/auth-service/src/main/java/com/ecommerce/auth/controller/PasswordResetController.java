package com.ecommerce.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.payload.request.ResetPasswordRequest;
import com.ecommerce.auth.payload.response.MessageResponse;
import com.ecommerce.auth.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);
    
    private final PasswordResetService passwordResetService;
    
    /**
     * Initiates password reset process by sending an email with reset link
     * @param email User's email address
     * @return Message response
     */
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        logger.info("Password reset request received for email: {}", email);
        
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is required"));
        }
        
        boolean sent = passwordResetService.initiatePasswordReset(email);
        
        if (sent) {
            // Always return success to prevent email enumeration attacks
            return ResponseEntity.ok(new MessageResponse(
                    "Nếu email của bạn tồn tại trong hệ thống, chúng tôi sẽ gửi cho bạn hướng dẫn khôi phục mật khẩu."));
        } else {
            // For security reasons, always return success even if email not found
            return ResponseEntity.ok(new MessageResponse(
                    "Nếu email của bạn tồn tại trong hệ thống, chúng tôi sẽ gửi cho bạn hướng dẫn khôi phục mật khẩu."));
        }
    }
    
    /**
     * Validates a password reset token
     * @param token Password reset token
     * @return User info if token is valid
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateResetToken(@RequestParam("token") String token) {
        logger.info("Validating password reset token: {}", token);
        
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Token is required"));
        }
        
        UserDto userDto = passwordResetService.validatePasswordResetToken(token);
        
        if (userDto != null) {
            // Return minimal user info (just username for UI display)
            return ResponseEntity.ok(userDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Liên kết đã hết hạn hoặc không hợp lệ."));
        }
    }
    
    /**
     * Resets password using valid token
     * @param resetRequest Reset password request with token and new password
     * @return Message response
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        logger.info("Processing password reset request");
        
        try {
            boolean result = passwordResetService.resetPassword(resetRequest);
            
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Mật khẩu của bạn đã được cập nhật thành công!"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Không thể đặt lại mật khẩu. Vui lòng thử lại."));
            }
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new MessageResponse(e.getReason()));
        } catch (Exception e) {
            logger.error("Error during password reset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Đã xảy ra lỗi khi xử lý yêu cầu của bạn."));
        }
    }
} 