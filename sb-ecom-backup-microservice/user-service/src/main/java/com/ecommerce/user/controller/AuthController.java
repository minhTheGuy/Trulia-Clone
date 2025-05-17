package com.ecommerce.user.controller;

import com.ecommerce.user.payload.dto.AuthUserDTO;
import com.ecommerce.user.payload.request.LoginRequest;
import com.ecommerce.user.payload.request.SignupRequest;
import com.ecommerce.user.model.User;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

@RestController("/api/users")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/validate")
    public ResponseEntity<AuthUserDTO> validateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Validating user credentials for: {}", loginRequest.getUsername());
        
        try {
            // Find user by username
            Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Verify password
                if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                    logger.info("User {} authenticated successfully", user.getUsername());
                    
                    // Convert to DTO
                    AuthUserDTO authUserDto = AuthUserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .active(user.isActive())
                            .roles(new HashSet<>(Collections.singletonList("ROLE_USER"))) // Simplify for now
                            .build();
                    
                    return ResponseEntity.ok(authUserDto);
                }
            }
            
            logger.warn("Authentication failed for user: {}", loginRequest.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            
        } catch (Exception e) {
            logger.error("Error validating user: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error validating user: " + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthUserDTO> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("Processing registration for user: {}", signupRequest.getUsername());
        
        try {
            // Check if username exists
            if (userService.existsByUsername(signupRequest.getUsername())) {
                logger.warn("Username already taken: {}", signupRequest.getUsername());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
            }
            
            // Check if email exists
            if (userService.existsByEmail(signupRequest.getEmail())) {
                logger.warn("Email already in use: {}", signupRequest.getEmail());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
            }
            
            // Create new user
            User user = new User();
            user.setUsername(signupRequest.getUsername());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setFirstName(signupRequest.getFirstName());
            user.setLastName(signupRequest.getLastName());
            user.setPhoneNumber(signupRequest.getPhoneNumber());
            user.setActive(true);
            
            User savedUser = userService.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());
            
            // Convert to DTO
            AuthUserDTO authUserDto = AuthUserDTO.builder()
                    .id(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .active(savedUser.isActive())
                    .roles(new HashSet<>(Collections.singletonList("ROLE_USER"))) // Simplify for now
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(authUserDto);
            
        } catch (ResponseStatusException e) {
            // Re-throw REST exceptions with their original status and message
            throw e;
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error registering user: " + e.getMessage());
        }
    }
} 