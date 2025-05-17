package com.ecommerce.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user.payload.dto.UpdateRoleDTO;
import com.ecommerce.user.payload.dto.UserDTO;
import com.ecommerce.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    /**
     * Get all users with pagination
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        logger.debug("Getting all users - page: {}, size: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<UserDTO> users = userService.getAllUsersPage(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get a specific user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user status (active/inactive)
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusDTO statusDTO) {
        
        UserDTO updatedUser = userService.updateUserStatus(id, statusDTO.isActive());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user role - add/remove ROLE_SELLER
     */
    @PutMapping("/users/{id}/seller-role")
    public ResponseEntity<UserDTO> updateSellerRole(
            @PathVariable Long id,
            @RequestBody SellerRoleDTO sellerRoleDTO) {
        
        UpdateRoleDTO updateRoleDTO = new UpdateRoleDTO();
        updateRoleDTO.setSellerRole(sellerRoleDTO.isSellerRole());
        
        // Keep existing broker role unchanged
        UserDTO user = userService.getUserById(id);
        boolean hasBrokerRole = user.getRoles().stream()
                .anyMatch(role -> role.equals("ROLE_BROKER"));
        updateRoleDTO.setBrokerRole(hasBrokerRole);
        
        UserDTO updatedUser = userService.updateUserRole(id, updateRoleDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Update user role - add/remove ROLE_BROKER
     */
    @PutMapping("/users/{id}/broker-role")
    public ResponseEntity<UserDTO> updateBrokerRole(
            @PathVariable Long id,
            @RequestBody BrokerRoleDTO brokerRoleDTO) {
        
        UpdateRoleDTO updateRoleDTO = new UpdateRoleDTO();
        
        // Keep existing seller role unchanged
        UserDTO user = userService.getUserById(id);
        boolean hasSellerRole = user.getRoles().stream()
                .anyMatch(role -> role.equals("ROLE_SELLER"));
        updateRoleDTO.setSellerRole(hasSellerRole);
        
        // Update broker role
        updateRoleDTO.setBrokerRole(brokerRoleDTO.isBrokerRole());
        
        UserDTO updatedUser = userService.updateUserRole(id, updateRoleDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update both seller and broker roles at once
     */
    @PutMapping("/users/{id}/roles")
    public ResponseEntity<UserDTO> updateUserRoles(
            @PathVariable Long id,
            @RequestBody UpdateRoleDTO updateRoleDTO) {
        
        UserDTO updatedUser = userService.updateUserRole(id, updateRoleDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * DTO for user status updates
     */
    static class UpdateStatusDTO {
        private boolean active;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
    
    /**
     * DTO for seller role updates
     */
    static class SellerRoleDTO {
        private boolean sellerRole;

        public boolean isSellerRole() {
            return sellerRole;
        }

        public void setSellerRole(boolean sellerRole) {
            this.sellerRole = sellerRole;
        }
    }
    
    /**
     * DTO for broker role updates
     */
    static class BrokerRoleDTO {
        private boolean brokerRole;

        public boolean isBrokerRole() {
            return brokerRole;
        }

        public void setBrokerRole(boolean brokerRole) {
            this.brokerRole = brokerRole;
        }
    }
} 