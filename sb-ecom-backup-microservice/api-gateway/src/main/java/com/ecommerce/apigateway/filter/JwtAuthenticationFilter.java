package com.ecommerce.apigateway.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.ecommerce.apigateway.config.Role;
import com.ecommerce.apigateway.util.JwtUtil;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.cookie.name:jwt-token}")
    private String jwtCookieName;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Updated exclusion list with proper patterns
    private static final List<String> EXCLUDED_URLS = List.of(
            "/",
            "/dashboard",
            "/info",
            "/api",
            "/actuator/**", 
            "/debug/**",
            "/api/auth/signin",
            "/api/auth/signup",
            "/api/auth/signout",
            "/api/auth/public/**",
            "/api/users/validate",
            "/api/users/register",
            "/api/users/signup",
            "/api/users/public/**",
            "/api/files/view/**",
            "/images/**",
            "/api/categories/public/**",
            "/api/properties/public/**",
            "/api/rentals/public/**",
            "/api/transactions/public/**", 
            "/api/favorites/public/**",
            "/api/auth/verify/**"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            
            logger.debug("Processing request for path: {}", path);

            // Skip authentication for excluded URLs
            if (isExcludedUrl(path)) {
                logger.debug("Path {} is excluded from authentication - skipping JWT verification", path);
                return chain.filter(exchange);
            }

            logger.debug("Path {} requires authentication", path);
            
            // Try to get JWT from various sources
            String token = extractJwtToken(exchange);
            
            if (token == null) {
                logger.warn("Request to {} rejected: No JWT token found in headers or cookies", path);
                return onError(exchange, "No JWT token found", HttpStatus.UNAUTHORIZED);
            }

            try {
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("Request to {} rejected: Token validation failed", path);
                    return onError(exchange, "Token is expired", HttpStatus.UNAUTHORIZED);
                }
                
                Claims claims = jwtUtil.getAllClaimsFromToken(token);
                String userId = claims.getSubject();
                
                // Extract user roles from token
                Set<String> userRoles = extractRoles(claims);
                String roleString = String.join(",", userRoles);
                
                logger.debug("Authenticated user {} with roles {} accessing {}", userId, roleString, path);
                
                // Check if required roles are provided and user has at least one of them
                if (config.requiredRoles != null && !config.requiredRoles.isEmpty()) {
                    boolean hasRequiredRole = false;
                    
                    for (String requiredRole : config.requiredRoles) {

                        // Ensure required role has ROLE_ prefix
                        if (!requiredRole.startsWith("ROLE_")) {
                            requiredRole = "ROLE_" + requiredRole;
                        }
                        
                        if (userRoles.contains(requiredRole)) {
                            hasRequiredRole = true;
                            break;
                        }
                    }
                    
                    if (!hasRequiredRole) {
                        logger.warn("Access denied for user {} - required roles: {}, user roles: {}", 
                                userId, config.requiredRoles, userRoles);
                        return onError(exchange, "Access denied - insufficient privileges", HttpStatus.FORBIDDEN);
                    }
                    
                    logger.debug("Role check passed for user {} accessing {}", userId, path);
                }
                
                // Add user information to headers for downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-UserId", userId)
                        .header("X-Auth-UserRoles", roleString)
                        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_PREFIX + token) // Pass the token to downstream services
                        .build();
                
                // Log the headers being sent to downstream services
                logger.debug("Headers being sent to downstream service:");
                logger.debug("  X-Auth-UserId: {}", userId);
                logger.debug("  X-Auth-UserRoles: {}", roleString);
                logger.debug("  Authorization: {}", AUTHORIZATION_PREFIX + token.substring(0, Math.min(10, token.length())) + "...");

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                logger.error("Authentication error for path {}: {}", path, e.getMessage(), e);
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }
    
    /**
     * Extract user roles from JWT claims
     */
    private Set<String> extractRoles(Claims claims) {
        Set<String> roles = new HashSet<>();
        Object rolesObj = claims.get("roles");
        
        if (rolesObj instanceof Collection) {
            // Handle roles as a collection
            roles = ((Collection<?>) rolesObj).stream()
                    .map(Object::toString)
                    .map(this::mapToValidRole)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            logger.debug("Extracted roles as Collection: {}", roles);
        } else if (rolesObj instanceof String) {
            // Handle roles as a comma-separated string
            String roleStr = (String) rolesObj;
            roles = Arrays.stream(roleStr.split(","))
                    .map(String::trim)
                    .map(this::mapToValidRole)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            logger.debug("Extracted roles as String: {}", roles);
        } else if (rolesObj != null) {
            // Handle as a single role
            String roleStr = rolesObj.toString();
            String validRole = mapToValidRole(roleStr);
            if (validRole != null) {
                roles.add(validRole);
            }
            logger.debug("Extracted roles as single value: {}", roles);
        }
        
        return roles;
    }

    private String mapToValidRole(String roleValue) {
        // Try to match with Role enum
        for (Role role : Role.values()) {
            // Check if matches the role string or roleId pattern
            if (roleValue.equals(role.name()) || 
                roleValue.equals(role.getRole()) || 
                roleValue.equalsIgnoreCase(role.name().replace("ROLE_", ""))) {
                
                // Always return with ROLE_ prefix for Spring Security
                return "ROLE_" + role.name();
            }
        }
        
        // Handle legacy format that might come directly from JWT
        if (roleValue.startsWith("ROLE_")) {
            String roleName = roleValue.substring(5); // Remove "ROLE_"
            try {
                Role role = Role.valueOf(roleName);
                return "ROLE_" + role.name();
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown role found in JWT: {}", roleValue);
                return null;
            }
        }
        
        logger.warn("Unable to map role value to a valid role: {}", roleValue);
        return null;
    }

    /**
     * Extract JWT token from request, checking both Authorization header and cookies
     */
    private String extractJwtToken(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        
        // First, try to get from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(AUTHORIZATION_PREFIX)) {
            logger.debug("Found JWT in Authorization header");
            return authHeader.substring(AUTHORIZATION_PREFIX.length());
        }
        
        // If not in header, try to get from cookies
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies != null && !cookies.isEmpty()) {
            List<HttpCookie> tokenCookies = cookies.get(jwtCookieName);
            if (tokenCookies != null && !tokenCookies.isEmpty()) {
                String cookieToken = tokenCookies.get(0).getValue();
                if (cookieToken != null && !cookieToken.isEmpty()) {
                    logger.debug("Found JWT in cookies");
                    return cookieToken;
                }
            }
        }
        
        logger.debug("No JWT token found in request");
        return null;
    }

    private boolean isExcludedUrl(String path) {
        // Use AntPathMatcher for more reliable path matching
        for (String pattern : EXCLUDED_URLS) {
            if (pathMatcher.match(pattern, path)) {
                logger.debug("Path '{}' excluded by pattern '{}'", path, pattern);
                return true;
            }
        }
        logger.debug("Path '{}' is not in exclusion list, authentication required", path);
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        
        logger.debug("Sending error response: {} with status: {}", message, status);
        
        return response.setComplete();
    }

    public static class Config {
        private List<String> requiredRoles;
        
        public Config() {
        }
        
        public List<String> getRequiredRoles() {
            return requiredRoles;
        }
        
        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
        }
    }
}