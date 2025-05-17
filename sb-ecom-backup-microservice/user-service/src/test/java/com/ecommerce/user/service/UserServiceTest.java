package com.ecommerce.user.service;

import com.ecommerce.user.payload.dto.UserCreationDTO;
import com.ecommerce.user.payload.dto.UserDTO;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceService userPreferenceService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreationDTO testUserCreationDTO;

    @BeforeEach
    public void setup() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();

        testUserCreationDTO = UserCreationDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    public void testCreateUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userPreferenceService.createDefaultPreferences(anyLong())).thenReturn(null);

        // Act
        UserDTO result = userService.createUser(testUserCreationDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userPreferenceService, times(1)).createDefaultPreferences(anyLong());
    }

    @Test
    public void testCreateUser_UsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(testUserCreationDTO);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    public void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
    }

    @Test
    public void testGetAllUsers() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .build();
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
    }

    @Test
    public void testUpdateUser_Success() {
        // Arrange
        UserDTO updateDto = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phoneNumber("1234567890")
                .build();
        
        User updatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phoneNumber("1234567890")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserDTO result = userService.updateUser(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("1234567890", result.getPhoneNumber());
    }
} 