package com.ecommerce.auth.controller;

import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.payload.request.ChangePasswordRequest;
import com.ecommerce.auth.payload.request.LoginRequest;
import com.ecommerce.auth.payload.request.SignupRequest;
import com.ecommerce.auth.payload.response.JwtResponse;
import com.ecommerce.auth.payload.response.MessageResponse;
import com.ecommerce.auth.security.jwt.JwtUtils;
import com.ecommerce.auth.security.services.AuthService;
import com.ecommerce.auth.security.services.AuthUtil;
import com.ecommerce.auth.security.services.UserDetailsImpl;
import com.ecommerce.auth.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final VerificationService verificationService;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String jwt = authService.login(loginRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(authService.getUserDetails(loginRequest.getUsername())).toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new JwtResponse(jwt, authService.getUserDetails(loginRequest.getUsername()))
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            logger.info("Registration request for username: {}", signupRequest.getUsername());

            UserDto userDto = authService.register(signupRequest);

            // Create and send verification email
            verificationService.createVerificationTokenAndSendEmail(userDto);

            logger.info("User {} registered successfully. Verification email sent.", signupRequest.getUsername());
            return ResponseEntity.ok(new MessageResponse("Đăng ký thành công! Vui lòng kiểm tra email để xác nhận tài khoản."));
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException
            throw e;
        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);

            // Handle specific error messages
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Username is already taken")) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Error: Username is already taken!"));
                }

                if (e.getMessage().contains("Email is already in use")) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Error: Email is already in use!"));
                }
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser(@RequestHeader(name = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            authService.logout(token);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtUtils.getCleanJwtCookie().toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader(name = "Authorization", required = false) String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);

                if (authService.validateToken(jwtToken)) {
                    String newToken = authService.refreshToken(jwtToken);

                    String username = authService.getUsernameFromToken(jwtToken);

                    UserDto userDto = authUtil.getUserByUsername(username);

                    if (userDto != null) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDto).toString());

                        return ResponseEntity.ok()
                                .headers(headers)
                                .body(new JwtResponse(newToken, userDto));
                    }
                }
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException
            throw e;
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Token refresh failed: " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(new MessageResponse(isValid ? "Token is valid" : "Token is invalid"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        logger.info("Processing email verification request with token: {}", token);

        try {
            boolean verified = verificationService.verifyAccount(token);

            if (verified) {
                logger.info("Email verification successful for token: {}", token);
                return ResponseEntity.ok(new MessageResponse("Xác nhận email thành công! Bạn có thể đăng nhập ngay bây giờ."));
            } else {
                logger.warn("Email verification failed for token: {}", token);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Xác nhận email thất bại. Liên kết không hợp lệ hoặc đã hết hạn."));
            }
        } catch (Exception e) {
            logger.error("Error during email verification: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Email verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            logger.info("Password change request for user: {}", username);
            
            UserDto userDto = authUtil.getUserByUsername(username);
            if (userDto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy tài khoản!"));
            }
            
            // Validate current password
            if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), userDto.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Mật khẩu hiện tại không đúng!"));
            }
            
            // Update password
            boolean updated = authUtil.updateUserPassword(userDto.getId(), 
                passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            
            if (updated) {
                return ResponseEntity.ok(new MessageResponse("Mật khẩu đã được cập nhật thành công!"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Không thể cập nhật mật khẩu. Vui lòng thử lại sau."));
            }
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi hệ thống. Vui lòng thử lại sau."));
        }
    }
} 