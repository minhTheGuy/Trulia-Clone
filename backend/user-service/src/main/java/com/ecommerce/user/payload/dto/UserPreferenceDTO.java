package com.ecommerce.user.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceDTO {
    private Long id;
    private String language;
    private String currency;
    private String theme;
    private boolean emailNotificationsEnabled;
    private boolean smsNotificationsEnabled;
    private boolean pushNotificationsEnabled;
    private String timezone;
} 