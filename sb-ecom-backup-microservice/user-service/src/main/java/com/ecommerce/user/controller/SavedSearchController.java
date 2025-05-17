package com.ecommerce.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user.payload.dto.SavedSearchDTO;
import com.ecommerce.user.payload.dto.SavedSearchRequestDTO;
import com.ecommerce.user.service.SavedSearchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{userId}/saved-searches")
@RequiredArgsConstructor
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    @GetMapping
    public ResponseEntity<List<SavedSearchDTO>> getSavedSearchesByUserId(@PathVariable Long userId) {
        List<SavedSearchDTO> savedSearches = savedSearchService.getSavedSearchesByUserId(userId);
        return ResponseEntity.ok(savedSearches);
    }

    @PostMapping
    public ResponseEntity<SavedSearchDTO> createSavedSearch(
            @PathVariable Long userId,
            @Valid @RequestBody SavedSearchRequestDTO savedSearchRequestDTO) {
        SavedSearchDTO savedSearch = savedSearchService.createSavedSearch(userId, savedSearchRequestDTO);
        return new ResponseEntity<>(savedSearch, HttpStatus.CREATED);
    }

    @DeleteMapping("/{savedSearchId}")
    public ResponseEntity<Void> deleteSavedSearch(
            @PathVariable Long userId,
            @PathVariable Long savedSearchId) {
        savedSearchService.deleteSavedSearch(userId, savedSearchId);
        return ResponseEntity.noContent().build();
    }
} 