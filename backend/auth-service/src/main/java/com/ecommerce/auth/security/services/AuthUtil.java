package com.ecommerce.auth.security.services;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.payload.dto.UserDto;

import lombok.RequiredArgsConstructor;

/**
 * Utility service providing authentication helper methods
 */
@Service
@RequiredArgsConstructor
public class AuthUtil {
    private static final Logger logger = LoggerFactory.getLogger(AuthUtil.class);
    
    private final UserServiceClient userServiceClient;


    public UserDetailsImpl convertToUserDetails(UserDto userDto, String password) {
        Set<String> roles = userDto.getRoles();
        return UserDetailsImpl.build(userDto, password);
    }

    public boolean activateUserAccount(Long userId) {
        try {
            logger.info("Activating user account with ID: {}", userId);
            var response = userServiceClient.activateUser(userId);
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
        } catch (Exception e) {
            logger.error("Error activating user account with ID {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public UserDto getUserByUsername(String username) {
        try {
            logger.info("Fetching user details for username: {}", username);
            var response = userServiceClient.getUserByUsername(username);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                logger.warn("User not found for username: {}", username);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching user details for username {}: {}", username, e.getMessage());
            return null;
        }
    }

    public UserDto validateUser(String username, String password) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }

    public UserDto createUser(String username, String email, String password, String firstName, String lastName, String phoneNumber) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /**
     * Gets user by email address
     * @param email Email to look up
     * @return User details or null if not found
     */
    public UserDto getUserByEmail(String email) {
        try {
            logger.info("Fetching user details for email: {}", email);
            var response = userServiceClient.getUserByEmail(email);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                logger.warn("User not found for email: {}", email);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching user details for email {}: {}", email, e.getMessage());
            return null;
        }
    }

    public UserDto getUserById(Long id) {
        try {
            logger.info("Fetching user details for id: {}", id);
            var response = userServiceClient.getUserById(id);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                logger.warn("User not found for id: {}", id);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching user details for id {}: {}", id, e.getMessage());
            return null;
        }
    }

    public boolean updateUserPassword(Long id, String newPassword) {
        try {
            logger.info("Updating password for user id: {}", id);
            var response = userServiceClient.updatePassword(id, newPassword);
            if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody())) {
                logger.info("Password updated successfully for user id: {}", id);
                return true;
            } else {
                logger.warn("Failed to update password for user id: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error updating password for user id {}: {}", id, e.getMessage());
            return false;
        }
    }
}