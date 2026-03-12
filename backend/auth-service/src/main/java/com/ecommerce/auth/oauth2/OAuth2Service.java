package com.ecommerce.auth.oauth2;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.payload.request.SignupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OAuth2Service {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserServiceClient userServiceClient;

    // Google OAuth2 config
    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth2.google.token-uri}")
    private String googleTokenUri;

    @Value("${oauth2.google.user-info-uri}")
    private String googleUserInfoUri;

    // GitHub OAuth2 config
    @Value("${oauth2.github.client-id}")
    private String githubClientId;

    @Value("${oauth2.github.client-secret}")
    private String githubClientSecret;

    @Value("${oauth2.github.redirect-uri}")
    private String githubRedirectUri;

    @Value("${oauth2.github.token-uri}")
    private String githubTokenUri;

    @Value("${oauth2.github.user-info-uri}")
    private String githubUserInfoUri;

    public String getAuthorizationUrl(String provider, String state) {
        return switch (provider.toLowerCase()) {
            case "google" -> buildGoogleAuthUrl(state);
            case "github" -> buildGithubAuthUrl(state);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        };
    }

    private String buildGoogleAuthUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .queryParam("access_type", "online")
                .build().toUriString();
    }

    private String buildGithubAuthUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl("https://github.com/login/oauth/authorize")
                .queryParam("client_id", githubClientId)
                .queryParam("redirect_uri", githubRedirectUri)
                .queryParam("scope", "user:email")
                .queryParam("state", state)
                .build().toUriString();
    }

    public UserDto processOAuthCallback(String provider, String code) {
        return switch (provider.toLowerCase()) {
            case "google" -> processGoogleCallback(code);
            case "github" -> processGithubCallback(code);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        };
    }

    @SuppressWarnings("unchecked")
    private UserDto processGoogleCallback(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", googleRedirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(googleTokenUri, tokenRequest, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            throw new RuntimeException("Failed to obtain Google access token");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(googleUserInfoUri, HttpMethod.GET, userRequest, Map.class);

        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new RuntimeException("Failed to fetch Google user info");
        }

        Map<String, Object> userInfo = userResponse.getBody();
        String email = (String) userInfo.get("email");
        String firstName = (String) userInfo.getOrDefault("given_name", "");
        String lastName = (String) userInfo.getOrDefault("family_name", "");
        String fullName = (String) userInfo.getOrDefault("name", firstName + " " + lastName);

        return findOrCreateUser(email, firstName, lastName, fullName);
    }

    @SuppressWarnings("unchecked")
    private UserDto processGithubCallback(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("redirect_uri", githubRedirectUri);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(githubTokenUri, tokenRequest, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            throw new RuntimeException("Failed to obtain GitHub access token");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(githubUserInfoUri, HttpMethod.GET, userRequest, Map.class);

        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new RuntimeException("Failed to fetch GitHub user info");
        }

        Map<String, Object> userInfo = userResponse.getBody();
        String login = (String) userInfo.get("login");
        String name = (String) userInfo.getOrDefault("name", login);
        String email = (String) userInfo.get("email");

        // GitHub may hide email from /user — fetch primary email separately
        if (email == null) {
            try {
                ResponseEntity<List> emailsResponse = restTemplate.exchange(
                        "https://api.github.com/user/emails", HttpMethod.GET, userRequest, List.class);
                if (emailsResponse.getStatusCode().is2xxSuccessful() && emailsResponse.getBody() != null) {
                    for (Object item : emailsResponse.getBody()) {
                        if (item instanceof Map<?, ?> entry
                                && Boolean.TRUE.equals(entry.get("primary"))
                                && Boolean.TRUE.equals(entry.get("verified"))
                                && entry.get("email") instanceof String emailVal) {
                            email = emailVal;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("Could not fetch GitHub emails: {}", ex.getMessage());
            }
        }

        if (email == null) {
            email = login + "@github.noreply.com";
        }

        String firstName = (name != null && name.contains(" ")) ? name.substring(0, name.indexOf(" ")) : (name != null ? name : login);
        String lastName = (name != null && name.contains(" ")) ? name.substring(name.indexOf(" ") + 1) : "";

        return findOrCreateUser(email, firstName, lastName, name != null ? name : login);
    }

    private UserDto findOrCreateUser(String email, String firstName, String lastName, String fullName) {
        // Try to find existing user by email
        try {
            ResponseEntity<UserDto> existingResponse = userServiceClient.getUserByEmail(email);
            if (existingResponse.getStatusCode().is2xxSuccessful() && existingResponse.getBody() != null) {
                UserDto existingUser = existingResponse.getBody();
                logger.info("Found existing OAuth2 user: {}", email);
                // Ensure the account is active
                if (!existingUser.isActive()) {
                    userServiceClient.activateUser(existingUser.getId());
                    existingUser.setActive(true);
                }
                return existingUser;
            }
        } catch (Exception e) {
            logger.info("No existing user found for email {}, creating new account", email);
        }

        // No existing user — create one
        String username = generateUniqueUsername(email, fullName);
        String password = UUID.randomUUID().toString();

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(username);
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName(firstName != null ? firstName : "");
        signupRequest.setLastName(lastName != null ? lastName : "");
        signupRequest.setPhoneNumber("");

        ResponseEntity<UserDto> createResponse = userServiceClient.createUser(signupRequest);

        if (!createResponse.getStatusCode().is2xxSuccessful() || createResponse.getBody() == null) {
            throw new RuntimeException("Failed to create user account for OAuth2 login");
        }

        UserDto newUser = createResponse.getBody();
        logger.info("Created new OAuth2 user: {}", email);

        // Activate immediately — OAuth provider already verified the email
        try {
            ResponseEntity<UserDto> activatedResponse = userServiceClient.activateUser(newUser.getId());
            if (activatedResponse.getStatusCode().is2xxSuccessful() && activatedResponse.getBody() != null) {
                return activatedResponse.getBody();
            }
        } catch (Exception e) {
            logger.warn("Could not activate user {}: {}", newUser.getId(), e.getMessage());
        }

        newUser.setActive(true);
        return newUser;
    }

    private String generateUniqueUsername(String email, String fullName) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (base.length() < 3) {
            base = base + "user";
        }
        if (base.length() > 18) {
            base = base.substring(0, 18);
        }

        // Check if base username is available
        try {
            userServiceClient.getUserByUsername(base);
            // Username taken, try with random suffix
            String candidate = base + "_" + ThreadLocalRandom.current().nextInt(100, 9999);
            return candidate.length() > 20 ? candidate.substring(0, 20) : candidate;
        } catch (Exception e) {
            return base;
        }
    }
}
