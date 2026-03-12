package com.ecommerce.auth.security.services;

import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.payload.request.LoginRequest;
import com.ecommerce.auth.payload.request.SignupRequest;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {
    /**
     * Authenticates a user with the provided credentials and returns a JWT token
     * @param loginRequest Login credentials
     * @return JWT token as string
     */
    String login(LoginRequest loginRequest);
    
    /**
     * Registers a new user with basic information
     * @param username Username for the new user
     * @param password Password for the new user
     * @return The registered user data
     */
    UserDto register(String username, String password);
    
    /**
     * Registers a new user with detailed information
     * @param signupRequest Signup request with user details
     * @return The registered user data
     */
    UserDto register(SignupRequest signupRequest);
    
    /**
     * Logs out a user by invalidating their token (for stateful implementations)
     * @param token JWT token to invalidate
     */
    void logout(String token);
    
    /**
     * Validates if a token is valid and not expired
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);
    
    /**
     * Refreshes an existing token to extend its lifetime
     * @param token Current valid JWT token
     * @return New JWT token
     */
    String refreshToken(String token);
    
    /**
     * Extracts the username from a token
     * @param token JWT token
     * @return Username contained in the token
     */
    String getUsernameFromToken(String token);

    UserDto getUserDetails(@NotBlank(message = "Username cannot be blank") String username);
}
