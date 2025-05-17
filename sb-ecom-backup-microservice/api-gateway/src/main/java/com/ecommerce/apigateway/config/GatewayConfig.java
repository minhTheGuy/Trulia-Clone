package com.ecommerce.apigateway.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.apigateway.filter.JwtAuthenticationFilter;

@Configuration
public class GatewayConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        logger.info("Configuring API Gateway routes");
        
        return builder.routes()
                // Root path - Handle by our IndexController
                .route("homepage", r -> r
                        .path("/", "/info", "/dashboard", "/actuator/**")
                        .uri("forward:/"))
                
                // Debug routes - No JWT filter
                .route("debug-routes", r -> r
                        .path("/debug/**")
                        .uri("forward:/"))
                        
                // Auth Service Routes - No JWT filter for authentication endpoints
                .route("auth-service-public", r -> r
                        .path("/api/auth/signin", "/api/auth/signup", "/api/auth/signout", "/api/auth/public/**", "/api/auth/password/forgot/**")
                        .filters(f -> {
                            logger.debug("Auth service public route matched");
                            return f;
                        })
                        .uri("lb://auth-service"))

                // Auth Service Routes with JWT filter
                .route("auth-service-protected", r -> r
                        .path("/api/auth/**")
                        .filters(f -> {
                            logger.debug("Auth service protected route matched");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://auth-service"))
                
                // User Service Routes - Other public endpoints
                .route("user-service-public", r -> r
                        .path("/api/users/public/**")
                        .filters(f -> {
                            logger.debug("User service public route matched");
                            return f;
                        })
                        .uri("lb://user-service"))
                
                // User Service Routes - Protected
                .route("user-service-protected", r -> r
                        .path("/api/users/**", "/api/addresses/**")
                        .filters(f -> {
                            logger.debug("User service protected route matched");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://user-service"))
                
                // Admin API Routes - Protected, requiring JWT and ROLE_ADMIN
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .filters(f -> {
                            logger.debug("Admin API route matched - routing to user-service");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            config.setRequiredRoles(List.of("ADMIN"));
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://user-service"))
                
                // File Service Routes - Public access for viewing files
                .route("file-service-public", r -> r
                        .path("/api/files/view/**", "/images/**")
                        .filters(f -> {
                            logger.debug("File service public route matched");
                            return f;
                        })
                        .uri("lb://file-service"))
                
                // File Service Routes - Protected endpoints
                .route("file-service-protected", r -> r
                        .path("/api/files/**")
                        .filters(f -> {
                            logger.debug("File service protected route matched");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://file-service"))
                
                // Property Service Routes - Including articles
                .route("property-service-public", r -> r
                        .path("/api/properties/public/**", "/api/rentals/public/**", "/api/favorites/public/**", "/api/property-articles/public/**")
                        .filters(f -> {
                            logger.debug("Property service public route matched");
                            return f;
                        })
                        .uri("lb://property-service"))
                
                .route("property-service-protected", r -> r
                        .path("/api/properties/**", "/api/rentals/**", "/api/favorites/**", "/api/property-articles/**")
                        .filters(f -> {
                            logger.debug("Property service protected route matched");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            config.setRequiredRoles(List.of("USER", "SELLER", "BROKER", "ADMIN"));
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://property-service"))
                
                // Seller API Routes - Protected, requiring JWT and ROLE_SELLER
                .route("seller-service", r -> r
                        .path("/api/seller/**")
                        .filters(f -> {
                            logger.debug("Seller API route matched");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            config.setRequiredRoles(List.of("SELLER"));
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://property-service"))
                
                // Category Service Routes - clearly separated and with debug logging
                .route("category-service-public", r -> r
                        .path("/api/categories/public/**")
                        .filters(f -> {
                            logger.debug("Category service public route matched - routing to category-service");
                            return f;
                        })
                        .uri("lb://category-service"))
                
                .route("category-service-protected", r -> r
                        .path("/api/categories/**")
                        .filters(f -> {
                            logger.debug("Category service protected route matched - routing to category-service");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://category-service"))
                        
                // Transaction Service Routes - Public Webhook endpoints
                .route("transaction-service-public", r -> r
                        .path("/api/webhook/**", "/api/transactions/public/**", "/api/payments/public/**")
                        .filters(f -> {
                            logger.debug("Transaction service public route matched - routing to transaction-service");
                            return f;
                        })
                        .uri("lb://transaction-service"))
                
                // Transaction Service Routes - Protected endpoints
                .route("transaction-service-protected", r -> r
                        .path("/api/transactions/**", "/api/payments/**")
                        .filters(f -> {
                            logger.debug("Transaction service protected route matched - routing to transaction-service");
                            JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
                            return f.filter(jwtAuthenticationFilter.apply(config));
                        })
                        .uri("lb://transaction-service"))
                .build();
    }
}