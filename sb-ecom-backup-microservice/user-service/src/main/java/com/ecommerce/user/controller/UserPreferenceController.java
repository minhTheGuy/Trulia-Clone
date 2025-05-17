package com.ecommerce.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user.payload.dto.UserPreferenceDTO;
import com.ecommerce.user.service.UserPreferenceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<UserPreferenceDTO> getUserPreferences(@PathVariable Long userId) {
        UserPreferenceDTO preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping
    public ResponseEntity<UserPreferenceDTO> updateUserPreferences(
            @PathVariable Long userId,
            @Valid @RequestBody UserPreferenceDTO preferencesDTO) {
        UserPreferenceDTO updatedPreferences = preferenceService.updateUserPreferences(userId, preferencesDTO);
        return ResponseEntity.ok(updatedPreferences);
    }
} 