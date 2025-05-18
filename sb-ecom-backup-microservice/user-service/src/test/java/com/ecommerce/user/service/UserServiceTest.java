package com.ecommerce.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.RoleName;
import com.ecommerce.user.model.User;
import com.ecommerce.user.payload.dto.UpdateRoleDTO;
import com.ecommerce.user.payload.dto.UserCreationDTO;
import com.ecommerce.user.payload.dto.UserDTO;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;


    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private UserCreationDTO userCreationDTO;
    private Role roleUser;
    private Role roleSeller;

    @BeforeEach
    public void setup() {
        // Setup roles
        roleUser = new Role();
        roleUser.setRoleId(0);
        roleUser.setRoleName(RoleName.ROLE_USER);

        roleSeller = new Role();
        roleSeller.setRoleId(2);
        roleSeller.setRoleName(RoleName.ROLE_SELLER);

        // Setup user
        Set<Role> roles = new HashSet<>();
        roles.add(roleUser);

        user = new User();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhoneNumber("1234567890");
        user.setRoles(roles);
        user.setActive(true);

        // Setup UserDTO
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setPhoneNumber("1234567890");
        userDTO.setRoles(Set.of("ROLE_USER"));
        userDTO.setActive(true);

        // Setup UserCreationDTO
        userCreationDTO = new UserCreationDTO();
        userCreationDTO.setFirstName("Test");
        userCreationDTO.setLastName("User");
        userCreationDTO.setUsername("testuser");
        userCreationDTO.setEmail("test@example.com");
        userCreationDTO.setPassword("password123");
        userCreationDTO.setPhoneNumber("1234567890");
    }

    @Test
    public void testCreateUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.createUser(userCreationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_USER"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    public void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    public void testGetAllUsers_Success() {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(user);
        when(userRepository.findAll()).thenReturn(users);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDTO.getUsername(), result.get(0).getUsername());
        assertEquals(userDTO.getUsername(), result.get(0).getUsername());
        assertEquals(userDTO.getEmail(), result.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }


    @Test
    public void testUpdateUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("Updated Name");
        updateDTO.setPhoneNumber("9876543210");

        // Act
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertNotEquals(updateDTO.getUsername(), result.getUsername());
        assertNotEquals(updateDTO.getPhoneNumber(), result.getPhoneNumber());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(modelMapper, times(1)).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    public void testDeleteUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void testUpdateSellerRole_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.of(roleUser));
        when(roleRepository.findByRoleName(RoleName.ROLE_SELLER)).thenReturn(Optional.of(roleSeller));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.updateSellerRole(1L, true);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findByRoleName(RoleName.ROLE_USER);
        verify(roleRepository, times(1)).findByRoleName(RoleName.ROLE_SELLER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdatePassword_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.updatePassword(1L, "newPassword");

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }
}

