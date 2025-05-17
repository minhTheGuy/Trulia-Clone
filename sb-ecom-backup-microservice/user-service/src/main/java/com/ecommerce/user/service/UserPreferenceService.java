package com.ecommerce.user.service;

import com.ecommerce.user.payload.dto.UserPreferenceDTO;

public interface UserPreferenceService {
    
    UserPreferenceDTO getUserPreferences(Long userId);
    
    UserPreferenceDTO updateUserPreferences(Long userId, UserPreferenceDTO preferencesDTO);
    
    UserPreferenceDTO createDefaultPreferences(Long userId);
} 