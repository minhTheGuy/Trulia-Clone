package com.ecommerce.auth.controller;

import com.ecommerce.auth.oauth2.OAuth2Service;
import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuthController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);

    @Autowired
    private OAuth2Service oAuth2Service;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${oauth2.frontend-redirect-uri}")
    private String frontendRedirectUri;

    /**
     * Redirects the browser to the OAuth2 provider's authorization page.
     * Example: GET /api/auth/oauth2/google/authorize
     */
    @GetMapping("/{provider}/authorize")
    public void authorize(@PathVariable String provider, HttpServletResponse response) throws IOException {
        try {
            String state = UUID.randomUUID().toString();
            String authorizationUrl = oAuth2Service.getAuthorizationUrl(provider, state);
            logger.info("Redirecting to {} OAuth2 authorization URL", provider);
            response.sendRedirect(authorizationUrl);
        } catch (IllegalArgumentException e) {
            logger.error("Unsupported OAuth2 provider: {}", provider);
            response.sendRedirect(frontendRedirectUri + "?error=" + encode("Unsupported provider: " + provider));
        }
    }

    /**
     * Handles the callback from the OAuth2 provider after user authorization.
     * Exchanges the code for a token, finds/creates the user, and redirects the browser
     * to the frontend with the JWT token as a query parameter.
     * Example: GET /api/auth/oauth2/google/callback?code=xxx&state=yyy
     */
    @GetMapping("/{provider}/callback")
    public void callback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            HttpServletResponse response) throws IOException {

        if (error != null) {
            String message = error_description != null ? error_description : error;
            logger.warn("OAuth2 provider returned error for {}: {}", provider, message);
            response.sendRedirect(frontendRedirectUri + "?error=" + encode(message));
            return;
        }

        if (code == null || code.isBlank()) {
            logger.warn("OAuth2 callback received without authorization code for provider: {}", provider);
            response.sendRedirect(frontendRedirectUri + "?error=" + encode("Authorization code not received"));
            return;
        }

        try {
            UserDto user = oAuth2Service.processOAuthCallback(provider, code);
            String token = jwtUtils.generateJwtToken(user);
            logger.info("OAuth2 login successful for user: {}", user.getEmail());
            response.sendRedirect(frontendRedirectUri + "?token=" + encode(token));
        } catch (Exception e) {
            logger.error("OAuth2 callback processing failed for provider {}: {}", provider, e.getMessage(), e);
            response.sendRedirect(frontendRedirectUri + "?error=" + encode("Authentication failed. Please try again."));
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
