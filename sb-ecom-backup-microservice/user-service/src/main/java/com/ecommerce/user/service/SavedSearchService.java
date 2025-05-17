package com.ecommerce.user.service;

import java.util.List;

import com.ecommerce.user.payload.dto.SavedSearchDTO;
import com.ecommerce.user.payload.dto.SavedSearchRequestDTO;

public interface SavedSearchService {
    List<SavedSearchDTO> getSavedSearchesByUserId(Long userId);
    
    SavedSearchDTO createSavedSearch(Long userId, SavedSearchRequestDTO savedSearchRequestDTO);
    
    void deleteSavedSearch(Long userId, Long savedSearchId);
} 