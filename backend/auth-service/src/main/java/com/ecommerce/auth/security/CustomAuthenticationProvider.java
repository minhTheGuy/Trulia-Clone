package com.ecommerce.auth.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.payload.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Component
@Primary
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
    
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        logger.info("Authenticating user: {}", username);
        
        try {
            // Lấy thông tin người dùng từ service
            ResponseEntity<UserDto> userResponse = userServiceClient.getUserByUsername(username);
            
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                logger.warn("User not found: {}", username);
                throw new BadCredentialsException("Invalid username or password");
            }
            
            UserDto userDto = userResponse.getBody();
            String encodedPassword = userDto.getPassword();
            
            // So sánh mật khẩu người dùng nhập với mật khẩu đã mã hóa
            if (encodedPassword != null && passwordEncoder.matches(password, encodedPassword)) {
                logger.info("User {} authenticated successfully with encoded password", username);
                
                // Convert roles to GrantedAuthority objects
                Set<String> roleSet = userDto.getRoles();
                List<GrantedAuthority> authorities = roleSet.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                
                // Create authentication token with user details and authorities
                return new UsernamePasswordAuthenticationToken(
                        username, 
                        encodedPassword, // Đặt mật khẩu đã mã hóa để sử dụng cho các yêu cầu xác thực trong tương lai
                        authorities
                );
            }
            
            logger.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Invalid username or password");
            
        } catch (Exception e) {
            logger.error("Error during authentication: {}", e.getMessage(), e);
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
} 