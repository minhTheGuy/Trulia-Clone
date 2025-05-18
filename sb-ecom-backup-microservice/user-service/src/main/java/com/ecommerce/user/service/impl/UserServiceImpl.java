package com.ecommerce.user.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.RoleName;
import com.ecommerce.user.model.User;
import com.ecommerce.user.payload.dto.AuthUserDTO;
import com.ecommerce.user.payload.dto.UpdateRoleDTO;
import com.ecommerce.user.payload.dto.UserCreationDTO;
import com.ecommerce.user.payload.dto.UserDTO;
import com.ecommerce.user.payload.request.LoginRequest;
import com.ecommerce.user.payload.request.SignupRequest;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO createUser(UserCreationDTO userCreationDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(userCreationDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + userCreationDTO.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userCreationDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + userCreationDTO.getEmail());
        }

        // Map DTO to entity
        User user = User.builder()
                .username(userCreationDTO.getUsername())
                .email(userCreationDTO.getEmail())
                .password(userCreationDTO.getPassword()) // In a real application, password should be encrypted
                .firstName(userCreationDTO.getFirstName())
                .lastName(userCreationDTO.getLastName())
                .phoneNumber(userCreationDTO.getPhoneNumber())
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Map entity to DTO and return
        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return convertToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserDTO> getAllUsersPage(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update user properties, but skip sensitive information like password
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());

        // Save updated user
        User updatedUser = userRepository.save(existingUser);

        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public AuthUserDTO validate(LoginRequest loginRequest) {

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    logger.info("Public endpoint: Validating user credentials for: {}",
                            loginRequest.getUsername());
                    return new ResourceNotFoundException("User not found with username: " + loginRequest.getUsername());
                });


        // Verify password
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.info("User {} authenticated successfully", user.getUsername());

            // Get user roles
            Set<String> roles = getUserRoles(user);

            // Convert to DTO
            AuthUserDTO authUserDto = AuthUserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())// Don't include password in DTO
                    .active(user.isActive())
                    .roles(roles)
                    .build();
        }

        return modelMapper.map(user, AuthUserDTO.class);
    }

    @Override
    public AuthUserDTO registerUser(SignupRequest signupRequest) {
        logger.info("Public endpoint: Processing registration for user: {} (from IP: {})",
                signupRequest.getUsername());
        if (existsByUsername(signupRequest.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        if (existsByEmail(signupRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setActive(false); // Set to inactive until email verification

        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (signupRequest.getRoles() != null && !signupRequest.getRoles().isEmpty()) {
            for (String roleName : signupRequest.getRoles()) {
                Optional<Role> role = roleRepository.findByRoleName(RoleName.valueOf(roleName));
                role.ifPresent(roles::add);
            }
        }

        if (roles.isEmpty()) {
            Optional<Role> userRole = roleRepository.findByRoleName(RoleName.ROLE_USER);
            userRole.ifPresent(roles::add);
        }

        user.setRoles(roles);

        User savedUser = save(user);

        return AuthUserDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .active(savedUser.isActive())
                .roles(getUserRoles(savedUser))
                .build();
    }

    @Override
    @Transactional
    public UserDTO updateUserRole(Long userId, UpdateRoleDTO updateRoleDTO) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Find the ROLE_USER, ROLE_SELLER, and ROLE_BROKER roles
        Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_USER not found"));

        Role sellerRole = roleRepository.findByRoleName(RoleName.ROLE_SELLER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_SELLER not found"));

        Role brokerRole = roleRepository.findByRoleName(RoleName.ROLE_BROKER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_BROKER not found"));

        // Get current roles
        Set<Role> currentRoles = new HashSet<>(user.getRoles());

        // Always ensure the user has the ROLE_USER role
        currentRoles.add(userRole);

        // Add or remove ROLE_SELLER based on the request
        if (updateRoleDTO.isSellerRole()) {
            currentRoles.add(sellerRole);
        } else {
            currentRoles.remove(sellerRole);
        }

        // Add or remove ROLE_BROKER based on the request
        if (updateRoleDTO.isBrokerRole()) {
            currentRoles.add(brokerRole);
        } else {
            currentRoles.remove(brokerRole);
        }

        // Update user roles
        user.setRoles(currentRoles);

        // Save the user
        User updatedUser = userRepository.save(user);

        // Return updated user DTO
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUserStatus(Long userId, boolean active) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update active status
        user.setActive(active);

        // Save the user
        User updatedUser = userRepository.save(user);

        // Return updated user DTO
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public AuthUserDTO activateUserAccount(Long id) {
        logger.info("Activating user account with ID: {}", id);

        try {
            // Get the user
            UserDTO userDTO = getUserById(id);

            if (userDTO == null) {
                logger.warn("User not found with ID: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            // Get the actual user entity
            Optional<User> userOpt = findByUsername(userDTO.getUsername());

            if (userOpt.isEmpty()) {
                logger.warn("User entity not found with username: {}", userDTO.getUsername());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            User user = userOpt.get();

            // Set user to active
            user.setActive(true);
            User savedUser = save(user);

            logger.info("User account activated successfully: {}", savedUser.getUsername());

            // Get user roles
            Set<String> roles = getUserRoles(savedUser);

            // Convert to DTO
            AuthUserDTO authUserDto = AuthUserDTO.builder()
                    .id(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .active(savedUser.isActive())
                    .roles(roles)
                    .build();

            return authUserDto;
        } catch (ResponseStatusException e) {
            // Re-throw REST exceptions with their original status and message
            throw e;
        } catch (Exception e) {
            logger.error("Error activating user account: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error activating user account: " + e.getMessage());
        }
    }

    @Override
    public AuthUserDTO getAuthUserById(Long id) {
        logger.info("Getting auth user by ID: {}", id);

        try {
            // Get the user
            UserDTO userDTO = getUserById(id);

            if (userDTO == null) {
                logger.warn("User not found with ID: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            // Get the actual user entity
            Optional<User> userOpt = findByUsername(userDTO.getUsername());

            if (userOpt.isEmpty()) {
                logger.warn("User entity not found with username: {}", userDTO.getUsername());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            User user = userOpt.get();

            // Get user roles
            Set<String> roles = getUserRoles(user);

            // Convert to AuthUserDto
            AuthUserDTO authUserDto = AuthUserDTO.builder()
                    .id(userDTO.getId())
                    .username(userDTO.getUsername())
                    .email(userDTO.getEmail())
                    .active(userDTO.isActive())
                    .roles(roles)
                    .build();

            return authUserDto;
        } catch (ResponseStatusException e) {
            // Re-throw REST exceptions with their original status and message
            throw e;
        } catch (Exception e) {
            logger.error("Error getting auth user by ID: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error getting auth user by ID: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserDTO updatePassword(Long userId, String newPassword) {
        logger.info("Updating password for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Encode the new password
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Update the password
        user.setPassword(encodedPassword);

        // Save the updated user
        User updatedUser = userRepository.save(user);

        logger.info("Password updated successfully for user: {}", user.getUsername());

        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO updateSellerRole(Long userId, boolean sellerRole) {
        logger.info("Updating seller role for user id: {} to {}", userId, sellerRole);

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Find the ROLE_USER and ROLE_SELLER roles
        Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_USER not found"));

        Role sellerRoleEntity = roleRepository.findByRoleName(RoleName.ROLE_SELLER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_SELLER not found"));

        // Get current roles
        Set<Role> currentRoles = new HashSet<>(user.getRoles());

        // Always ensure the user has the ROLE_USER role
        currentRoles.add(userRole);

        // Add or remove ROLE_SELLER based on the request
        if (sellerRole) {
            currentRoles.add(sellerRoleEntity);
            logger.debug("Adding ROLE_SELLER to user: {}", user.getUsername());
        } else {
            currentRoles.remove(sellerRoleEntity);
            logger.debug("Removing ROLE_SELLER from user: {}", user.getUsername());
        }

        // Update user roles
        user.setRoles(currentRoles);

        // Save the user
        User updatedUser = userRepository.save(user);
        logger.info("Successfully updated seller role for user: {}", user.getUsername());

        // Return updated user DTO
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO updateBrokerRole(Long userId, boolean brokerRole) {
        logger.info("Updating broker role for user id: {} to {}", userId, brokerRole);

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Find the ROLE_USER and ROLE_BROKER roles
        Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_USER not found"));

        Role brokerRoleEntity = roleRepository.findByRoleName(RoleName.ROLE_BROKER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_BROKER not found"));

        // Get current roles
        Set<Role> currentRoles = new HashSet<>(user.getRoles());

        // Always ensure the user has the ROLE_USER role
        currentRoles.add(userRole);

        // Add or remove ROLE_BROKER based on the request
        if (brokerRole) {
            currentRoles.add(brokerRoleEntity);
            logger.debug("Adding ROLE_BROKER to user: {}", user.getUsername());
        } else {
            currentRoles.remove(brokerRoleEntity);
            logger.debug("Removing ROLE_BROKER from user: {}", user.getUsername());
        }

        // Update user roles
        user.setRoles(currentRoles);

        // Save the user
        User updatedUser = userRepository.save(user);
        logger.info("Successfully updated broker role for user: {}", user.getUsername());

        // Return updated user DTO
        return convertToDTO(updatedUser);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);

        // Map roles from Role entities to role name strings
        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getRoleName().name())
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        }

        return dto;
    }

    private Set<String> getUserRoles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());
    }
}
