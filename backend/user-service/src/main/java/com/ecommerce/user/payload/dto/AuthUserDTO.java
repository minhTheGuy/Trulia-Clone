package com.ecommerce.user.payload.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDTO {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean active;
} 