package com.ecommerce.user.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.user.model.User;
import com.ecommerce.user.payload.dto.AuthUserDTO;
import com.ecommerce.user.payload.dto.UserDTO;
import com.ecommerce.user.payload.request.LoginRequest;
import com.ecommerce.user.payload.request.SignupRequest;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/public")
@RequiredArgsConstructor
public class PublicAuthController {
    private static final Logger logger = LoggerFactory.getLogger(PublicAuthController.class);

    private final UserService userService;
    private final RoleRepository roleRepository;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck(HttpServletRequest request) {
        logger.info("Health check called from: {}", request.getRemoteAddr());
        return ResponseEntity.ok("PublicAuthController is healthy");
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthUserDTO> getUserById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Public endpoint: Getting user by ID: {} (from IP: {})", id, request.getRemoteAddr());

        try {
            AuthUserDTO authUserDto = userService.getAuthUserById(id);
            return ResponseEntity.ok(authUserDto);
        } catch (ResponseStatusException e) {
            // Re-throw REST exceptions with their original status and message
            throw e;
        } catch (Exception e) {
            logger.error("Error getting user by ID: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error getting user by ID: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<AuthUserDTO> validateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthUserDTO authUserDTO = userService.validate(loginRequest);
        if (authUserDTO != null) {
            return ResponseEntity.ok(authUserDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthUserDTO> registerUser(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request) {

        AuthUserDTO authUserDto = userService.registerUser(signupRequest);
        logger.info("User registered successfully: {}", authUserDto.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(authUserDto);
    }

    /**
     * Activates a user account
     */
    @PutMapping("/activate/{id}")
    public ResponseEntity<AuthUserDTO> activateUserAccount(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Public endpoint: Activating user account with ID: {} (from IP: {})",
                id, request.getRemoteAddr());

        try {
            AuthUserDTO authUserDto = userService.activateUserAccount(id);
            return ResponseEntity.ok(authUserDto);
        } catch (ResponseStatusException e) {
            // Re-throw REST exceptions with their original status and message
            throw e;
        } catch (Exception e) {
            logger.error("Error activating user account: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error activating user account: " + e.getMessage());
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO userDTO = userService.getUserByUsername(username);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO userDTO = userService.getUserByEmail(email);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/password/{id}")
    public ResponseEntity<Boolean> updatePassword(@PathVariable Long id, @RequestParam String password) {
        try {
            UserDTO updatedUser = userService.updatePassword(id, password);
            return ResponseEntity.ok(updatedUser != null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    private Set<String> getUserRoles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());
    }
} 