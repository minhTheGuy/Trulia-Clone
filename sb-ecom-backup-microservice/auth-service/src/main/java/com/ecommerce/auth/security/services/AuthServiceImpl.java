package com.ecommerce.auth.security.services;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.payload.request.LoginRequest;
import com.ecommerce.auth.payload.request.SignupRequest;
import com.ecommerce.auth.security.jwt.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtUtils jwtUtils;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String login(LoginRequest loginRequest) {
        try {
            logger.info("Authentication request for user: {}", loginRequest.getUsername());
            logger.debug("Attempting direct authentication with user-service...");
            ResponseEntity<UserDto> response = userServiceClient.validateUser(loginRequest);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserDto userDto = response.getBody();
                
                // Check if user is active
                if (!userDto.isActive()) {
                    logger.warn("User {} account is not activated", userDto.getUsername());
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                            "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email để xác nhận tài khoản.");
                }
                
                // Log thông tin vai trò để debug
                logger.debug("User roles from user-service: {}", userDto.getRoles());

                // Tạo JWT token
                String jwt = jwtUtils.generateJwtToken(userDto);
                logger.info("User {} authenticated successfully via direct user-service call", userDto.getUsername());

                return jwt;
            } else {
                logger.warn("Authentication failed for user: {}", loginRequest.getUsername());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }
        } catch (Exception e) {
            logger.error("Error during authentication: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public UserDto register(String username, String password) {
        return this.register(createBasicSignupRequest(username, password));
    }

    @Override
    public UserDto register(SignupRequest signupRequest) {
        try {
            logger.info("Registration request for user: {}", signupRequest.getUsername());
            
            // Gọi user-service để tạo người dùng
            logger.debug("Calling user-service createUser endpoint for user: {}", signupRequest.getUsername());
            ResponseEntity<UserDto> response = userServiceClient.createUser(signupRequest);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("User {} registered successfully", signupRequest.getUsername());
                return response.getBody();
            } else {
                logger.warn("Registration failed for user: {}", signupRequest.getUsername());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User registration failed");
            }
        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }

    private SignupRequest createBasicSignupRequest(String username, String password) {
        SignupRequest request = new SignupRequest();
        request.setUsername(username);
        request.setPassword(password);
        // Set a default role if needed
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        request.setRoles(roles);
        return request;
    }

    @Override
    public void logout(String token) {
        // JWTs là stateless nên không cần hủy token
        logger.info("User logged out successfully");
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtils.validateJwtToken(token);
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String refreshToken(String token) {
        // Kiểm tra token có hợp lệ không
        if (!validateToken(token)) {
            logger.warn("Cannot refresh an invalid token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        
        try {
            // Lấy username từ token
            String username = jwtUtils.getUserNameFromJwtToken(token);

            if (username == null) {
                UserDto userDto = userServiceClient.getUserByUsername(username).getBody();
                
                // Tạo token mới
                String newToken = jwtUtils.generateJwtToken(userDto);
                logger.info("Token refreshed successfully for user: {}", username);
                
                return newToken;
            } else {
                logger.warn("User not found during token refresh: {}", username);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Error during token refresh: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        try {
            return jwtUtils.getUserNameFromJwtToken(token);
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to extract username from token");
        }
    }

    @Override
    public UserDto getUserDetails(String username) {
        try {
            ResponseEntity<UserDto> response = userServiceClient.getUserByUsername(username);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                logger.warn("User not found: {}", username);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            logger.error("Error fetching user details: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch user details");
        }
    }
}
