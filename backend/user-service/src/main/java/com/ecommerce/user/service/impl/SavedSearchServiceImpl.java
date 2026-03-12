package com.ecommerce.user.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.SavedSearch;
import com.ecommerce.user.model.User;
import com.ecommerce.user.payload.dto.SavedSearchDTO;
import com.ecommerce.user.payload.dto.SavedSearchRequestDTO;
import com.ecommerce.user.repository.SavedSearchRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.SavedSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedSearchServiceImpl implements SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<SavedSearchDTO> getSavedSearchesByUserId(Long userId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        // Get saved searches
        List<SavedSearch> savedSearches = savedSearchRepository.findByUserId(userId);
        
        // Convert to DTOs
        return savedSearches.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SavedSearchDTO createSavedSearch(Long userId, SavedSearchRequestDTO savedSearchRequestDTO) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        try {
            // Convert criteria object to JSON string
            String criteriaJson = objectMapper.writeValueAsString(savedSearchRequestDTO.getCriteria());
            
            // Create saved search entity
            SavedSearch savedSearch = SavedSearch.builder()
                    .name(savedSearchRequestDTO.getName())
                    .criteria(criteriaJson)
                    .user(user)
                    .build();
            
            // Save to database
            SavedSearch savedSearchResult = savedSearchRepository.save(savedSearch);
            
            // Return DTO
            return mapToDTO(savedSearchResult);
        } catch (Exception e) {
            log.error("Error creating saved search", e);
            throw new RuntimeException("Error creating saved search: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteSavedSearch(Long userId, Long savedSearchId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        // Check if saved search exists and belongs to the user
        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found with id: " + savedSearchId));
        
        if (!savedSearch.getUser().getId().equals(userId)) {
            throw new RuntimeException("User is not authorized to delete this saved search");
        }
        
        // Delete saved search
        savedSearchRepository.deleteById(savedSearchId);
    }
    
    /**
     * Maps SavedSearch entity to SavedSearchDTO
     */
    private SavedSearchDTO mapToDTO(SavedSearch savedSearch) {
        try {
            // Parse JSON criteria string back to object
            Object criteriaObj = objectMapper.readValue(savedSearch.getCriteria(), Object.class);
            
            return SavedSearchDTO.builder()
                    .id(savedSearch.getId())
                    .name(savedSearch.getName())
                    .criteria(criteriaObj)
                    .createdAt(savedSearch.getCreatedAt())
                    .userId(savedSearch.getUser().getId())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping saved search to DTO", e);
            // Return basic DTO without criteria if error occurs
            return SavedSearchDTO.builder()
                    .id(savedSearch.getId())
                    .name(savedSearch.getName())
                    .createdAt(savedSearch.getCreatedAt())
                    .userId(savedSearch.getUser().getId())
                    .build();
        }
    }
} 