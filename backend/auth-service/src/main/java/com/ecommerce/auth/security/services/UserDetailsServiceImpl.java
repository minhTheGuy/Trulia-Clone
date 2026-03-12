package com.ecommerce.auth.security.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.payload.dto.UserDto;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user details for username: {}", username);
        
        try {
            logger.debug("Calling userServiceClient.getUserByUsername for: {}", username);

            ResponseEntity<UserDto> response = userServiceClient.getUserByUsername(username);

            logger.debug("Response received with status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserDto userDto = response.getBody();
                logger.info("Successfully loaded user details for: {}", username);
                
                // Sử dụng password mã hóa từ user-service
                String encodedPassword = userDto.getPassword();
                logger.debug("Using encoded password from user-service for authentication");
                
                return UserDetailsImpl.build(userDto, encodedPassword);
            } else {
                logger.warn("User not found with username: {}", username);
                throw new UsernameNotFoundException("User Not Found with username: " + username);
            }
        } catch (Exception e) {
            logger.error("Error loading user by username ({}): {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage());
        }
    }
} 