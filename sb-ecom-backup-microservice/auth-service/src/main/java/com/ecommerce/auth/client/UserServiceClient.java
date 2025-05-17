package com.ecommerce.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.auth.payload.dto.UserDto;
import com.ecommerce.auth.payload.request.LoginRequest;
import com.ecommerce.auth.payload.request.SignupRequest;

@FeignClient(name = "user-service", path = "/api/users/public")
public interface UserServiceClient {
    
    @PostMapping("/validate")
    ResponseEntity<UserDto> validateUser(@RequestBody LoginRequest loginRequest);
    
    @PostMapping("/register")
    ResponseEntity<UserDto> createUser(@RequestBody SignupRequest signupRequest);

    @GetMapping("/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id);
    
    @PutMapping("/activate/{id}")
    ResponseEntity<UserDto> activateUser(@PathVariable("id") Long id);

    @GetMapping("/username/{username}")
    ResponseEntity<UserDto> getUserByUsername(@PathVariable String username);
    
    @GetMapping("/email/{email}")
    ResponseEntity<UserDto> getUserByEmail(@PathVariable String email);
    
    @PutMapping("/password/{id}")
    ResponseEntity<Boolean> updatePassword(@PathVariable("id") Long id, @RequestParam("password") String newPassword);

    @PostMapping("/verify")
    ResponseEntity<Boolean> activateUserAccount(Long userId);
}