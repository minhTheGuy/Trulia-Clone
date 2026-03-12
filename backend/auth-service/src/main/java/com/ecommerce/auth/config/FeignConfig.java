package com.ecommerce.auth.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

@Configuration
public class FeignConfig {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FeignConfig.class);

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            logger.debug("Adding headers to Feign request: {}", requestTemplate.url());
            
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("Content-Type", "application/json");
            
            // Add service identification
            requestTemplate.header("X-Calling-Service", "auth-service");

            // Add header to identify internal service-to-service calls
            requestTemplate.header("X-Internal-Call", "true");
        };
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            String requestUrl = response.request().url();
            
            try {
                // Try to read the response body for better error messages
                String responseBody = null;
                if (response.body() != null) {
                    byte[] bodyData = response.body().asInputStream().readAllBytes();
                    responseBody = new String(bodyData, StandardCharsets.UTF_8);
                }
                
                logger.error("Error in Feign client call to {}: Status {}, Response: {}", requestUrl, status, responseBody);
                
                if (status == 403) {
                    // CORS issue or forbidden
                    logger.error("CORS issue or forbidden: URL={}, Method={}, Headers={}", 
                        requestUrl, 
                        response.request().httpMethod(),
                        response.request().headers());
                    return new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            String.format("Client error calling %s: %s - This may be a CORS issue", requestUrl, status));
                } else if (status == 404) {
                    // Endpoint not found
                    logger.error("Endpoint not found: URL={}, Method={}", 
                        requestUrl, response.request().httpMethod());
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("Endpoint not found at %s: %s", requestUrl, status));
                } else if (status >= 400 && status < 500) {
                    return new ResponseStatusException(
                            HttpStatus.valueOf(status),
                            String.format("Client error calling %s: %s - %s", requestUrl, status, responseBody));
                } else if (status >= 500) {
                    return new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            String.format("Server error calling %s: %s - %s", requestUrl, status, responseBody));
                }
                
                return new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        String.format("Unknown error calling %s: %s - %s", requestUrl, status, responseBody));
            } catch (IOException e) {
                logger.error("Error reading response body: {}", e.getMessage(), e);
                return new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        String.format("Error calling %s: %s", requestUrl, status));
            }
        };
    }
} 