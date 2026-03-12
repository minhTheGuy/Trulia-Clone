package com.ecommerce.property.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDTO {
    private String name;
    private String phone;
    private String email;
    private String image;
    private String company;
} 