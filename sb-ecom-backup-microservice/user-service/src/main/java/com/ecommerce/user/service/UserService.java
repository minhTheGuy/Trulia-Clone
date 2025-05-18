package com.ecommerce.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.user.model.User;
import com.ecommerce.user.payload.dto.AuthUserDTO;
import com.ecommerce.user.payload.dto.UpdateRoleDTO;
import com.ecommerce.user.payload.dto.UserCreationDTO;
import com.ecommerce.user.payload.dto.UserDTO;
import com.ecommerce.user.payload.request.LoginRequest;
import com.ecommerce.user.payload.request.SignupRequest;

import jakarta.validation.Valid;

public interface UserService {
    
    UserDTO createUser(UserCreationDTO userCreationDTO);
    
    UserDTO getUserById(Long id);
    
    UserDTO getUserByUsername(String username);
    
    UserDTO getUserByEmail(String email);
    
    List<UserDTO> getAllUsers();
    
    // Add paginated version for admin panel
    Page<UserDTO> getAllUsersPage(Pageable pageable);
    
    UserDTO updateUser(Long id, UserDTO userDTO);
    
    void deleteUser(Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Role management
    UserDTO updateUserRole(Long userId, UpdateRoleDTO updateRoleDTO);
    
    // Single role updates
    UserDTO updateSellerRole(Long userId, boolean sellerRole);

    UserDTO updateBrokerRole(Long userId, boolean brokerRole);

    // User status management for admin
    UserDTO updateUserStatus(Long userId, boolean active);
    
    // Password management
    UserDTO updatePassword(Long userId, String newPassword);
    
    // New methods for auth service
    Optional<User> findByUsername(String username);
    
    User save(User user);

    AuthUserDTO validate(@Valid LoginRequest loginRequest);

    AuthUserDTO registerUser(@Valid SignupRequest signupRequest);

    AuthUserDTO activateUserAccount(Long id);

    AuthUserDTO getAuthUserById(Long id);
}

