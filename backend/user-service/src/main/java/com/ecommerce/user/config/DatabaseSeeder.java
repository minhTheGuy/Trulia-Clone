package com.ecommerce.user.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.RoleName;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting database initialization...");
        seedRoles();
        seedUsers();
        log.info("Database initialization completed.");
    }

    private void seedRoles() {
        log.info("Seeding roles...");
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = new Role(roleName);
                roleRepository.save(role);
                log.info("Role {} created", roleName);
            }
        }
    }

    private void seedUsers() {
        log.info("Seeding admin user...");
        
        // Create admin account if not exists
        if (!userRepository.existsByUsername("admin")) {
            // Get roles
            Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            Role sellerRole = roleRepository.findByRoleName(RoleName.ROLE_SELLER)
                    .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));
            Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

            // Create admin user with all roles
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(userRole);
            adminRoles.add(sellerRole);
            adminRoles.add(adminRole);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .phoneNumber("0123456789")
                    .active(true)
                    .roles(adminRoles)
                    .build();

            userRepository.save(admin);
            log.info("Admin user created: {}", admin.getUsername());
        } else {
            log.info("Admin user already exists");
        }
    }
} 