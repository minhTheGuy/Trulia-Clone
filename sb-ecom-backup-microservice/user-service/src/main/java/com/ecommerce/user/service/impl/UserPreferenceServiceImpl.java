package com.ecommerce.user.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.payload.dto.UserPreferenceDTO;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserPreference;
import com.ecommerce.user.repository.UserPreferenceRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserPreferenceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserPreferenceDTO getUserPreferences(Long userId) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preferences not found for user with id: " + userId));
        
        return modelMapper.map(preference, UserPreferenceDTO.class);
    }

    @Override
    @Transactional
    public UserPreferenceDTO updateUserPreferences(Long userId, UserPreferenceDTO preferencesDTO) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preferences not found for user with id: " + userId));
        
        // Update preference fields
        preference.setLanguage(preferencesDTO.getLanguage());
        preference.setCurrency(preferencesDTO.getCurrency());
        preference.setTheme(preferencesDTO.getTheme());
        preference.setEmailNotificationsEnabled(preferencesDTO.isEmailNotificationsEnabled());
        preference.setSmsNotificationsEnabled(preferencesDTO.isSmsNotificationsEnabled());
        preference.setPushNotificationsEnabled(preferencesDTO.isPushNotificationsEnabled());
        preference.setTimezone(preferencesDTO.getTimezone());
        
        UserPreference updatedPreference = preferenceRepository.save(preference);
        return modelMapper.map(updatedPreference, UserPreferenceDTO.class);
    }

    @Override
    @Transactional
    public UserPreferenceDTO createDefaultPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if preferences already exist for this user
        if (preferenceRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Preferences already exist for user with id: " + userId);
        }
        
        // Create default preferences - defaults are set in @PrePersist in the entity
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        
        // Save and return
        UserPreference savedPreference = preferenceRepository.save(preference);
        return modelMapper.map(savedPreference, UserPreferenceDTO.class);
    }
} 