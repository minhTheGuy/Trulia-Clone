package com.ecommerce.user.controller;

import com.ecommerce.user.payload.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.user.service.UserService;
import com.ecommerce.user.service.RevenueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final RevenueService revenueService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        logger.debug("Getting all users - page: {}, size: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<UserDTO> users = userService.getAllUsersPage(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusDTO statusDTO) {
        
        UserDTO updatedUser = userService.updateUserStatus(id, statusDTO.isActive());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/users/{id}/seller-role")
    public ResponseEntity<UserDTO> updateSellerRole(
            @PathVariable Long id,
            @RequestBody SellerRoleDTO sellerRoleDTO) {
        
        UserDTO updatedUser = userService.updateSellerRole(id, sellerRoleDTO.isSellerRole());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/users/{id}/broker-role")
    public ResponseEntity<UserDTO> updateBrokerRole(
            @PathVariable Long id,
            @RequestBody BrokerRoleDTO brokerRoleDTO) {
        
        UserDTO updatedUser = userService.updateBrokerRole(id, brokerRoleDTO.isBrokerRole());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<UserDTO> updateUserRoles(
            @PathVariable Long id,
            @RequestBody UpdateRoleDTO updateRoleDTO) {
        
        UserDTO updatedUser = userService.updateUserRole(id, updateRoleDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueStatisticsDTO> getRevenueStatistics(
            @RequestParam String timeRange,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter) {
        
        logger.debug("Getting revenue statistics - timeRange: {}, year: {}, month: {}, quarter: {}", 
                    timeRange, year, month, quarter);
        
        RevenueStatisticsDTO statistics = revenueService.getRevenueStatistics(timeRange, year, month, quarter);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/revenue/by-source")
    public ResponseEntity<RevenueBySourceDTO> getRevenueBySource(
            @RequestParam String timeRange,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter) {
        
        logger.debug("Getting revenue by source - timeRange: {}, year: {}, month: {}, quarter: {}", 
                    timeRange, year, month, quarter);
        
        RevenueBySourceDTO revenueBySource = revenueService.getRevenueBySource(timeRange, year, month, quarter);
        return ResponseEntity.ok(revenueBySource);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDTO>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        logger.debug("Getting transactions - page: {}, size: {}, sort: {}", page, size, sort);
        
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<TransactionDTO> transactions = revenueService.getTransactions(pageable);
        
        return ResponseEntity.ok(transactions);
    }
}

