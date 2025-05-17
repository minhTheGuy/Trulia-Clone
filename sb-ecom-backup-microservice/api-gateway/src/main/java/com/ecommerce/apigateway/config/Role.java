package com.ecommerce.apigateway.config;

public enum Role {
    USER("Role(roleId=1)"),
    SELLER("Role(roleId=2)"),
    BROKER("Role(roleId=3)"),
    ADMIN("Role(roleId=4)");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
