package com.ecommerce.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.SavedSearch;
import com.ecommerce.user.model.User;
import com.ecommerce.user.payload.dto.SavedSearchDTO;
import com.ecommerce.user.payload.dto.SavedSearchRequestDTO;
import com.ecommerce.user.repository.SavedSearchRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.impl.SavedSearchServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SavedSearchServiceTest {

    @Mock
    private SavedSearchRepository savedSearchRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SavedSearchServiceImpl savedSearchService;

    private User user;
    private SavedSearch savedSearch;
    private SavedSearchDTO savedSearchDTO;
    private SavedSearchRequestDTO savedSearchRequestDTO;

    @BeforeEach
    public void setup() {
        // Setup user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setActive(true);

        // Setup saved search
        savedSearch = SavedSearch.builder()
                .id(1L)
                .user(user)
                .name("Test Search")
                .createdAt(LocalDateTime.now())
                .criteria("Biet Thu")
                .build();

        // Setup SavedSearchDTO
        savedSearchDTO = SavedSearchDTO.builder()
                .id(1L)
                .name("Test Search")
                .createdAt(LocalDateTime.now())
                .criteria("Biet Thu")
                .build();

    }

    @Test
    public void testGetSavedSearchesByUserId_Success() {
        // Arrange
        List<SavedSearch> savedSearches = new ArrayList<>();
        savedSearches.add(savedSearch);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(savedSearchRepository.findByUserId(anyLong())).thenReturn(savedSearches);

        // Act
        List<SavedSearchDTO> result = savedSearchService.getSavedSearchesByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(savedSearchDTO.getName(), result.get(0).getName());
        verify(userRepository, times(1)).findById(1L);
        verify(savedSearchRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testGetSavedSearchesByUserId_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> savedSearchService.getSavedSearchesByUserId(1L));
        verify(userRepository, times(1)).findById(1L);
        verify(savedSearchRepository, never()).findByUserId(anyLong());
    }

    @Test
    public void testCreateSavedSearch_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(savedSearchRepository.save(any(SavedSearch.class))).thenReturn(savedSearch);

        // Act
        SavedSearchDTO result = savedSearchService.createSavedSearch(1L, savedSearchRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(savedSearchDTO.getName(), result.getName());
        verify(userRepository, times(1)).findById(1L);
        verify(savedSearchRepository, times(1)).save(any(SavedSearch.class));
    }

    @Test
    public void testCreateSavedSearch_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            savedSearchService.createSavedSearch(1L, savedSearchRequestDTO));
        verify(userRepository, times(1)).findById(1L);
        verify(savedSearchRepository, never()).save(any(SavedSearch.class));
    }

    @Test
    public void testDeleteSavedSearch_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(savedSearchRepository.findById(anyLong())).thenReturn(Optional.of(savedSearch));
        doNothing().when(savedSearchRepository).delete(any(SavedSearch.class));

        // Act
        savedSearchService.deleteSavedSearch(1L, 1L);

        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(savedSearchRepository, times(1)).findById(1L);
        verify(savedSearchRepository, times(1)).delete(savedSearch);
    }

    @Test
    public void testDeleteSavedSearch_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            savedSearchService.deleteSavedSearch(1L, 1L));
        verify(userRepository, times(1)).findById(1L);
        verify(savedSearchRepository, never()).findById(anyLong());
        verify(savedSearchRepository, never()).delete(any(SavedSearch.class));
    }

    @Test
    public void testDeleteSavedSearch_SavedSearchNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(savedSearchRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            savedSearchService.deleteSavedSearch(1L, 1L));
        verify(userRepository, times(1)).findById(1L);
        verify(savedSearchRepository, times(1)).findById(1L);
        verify(savedSearchRepository, never()).delete(any(SavedSearch.class));
    }
} 