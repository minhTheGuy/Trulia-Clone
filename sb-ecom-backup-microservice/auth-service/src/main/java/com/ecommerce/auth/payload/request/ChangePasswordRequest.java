package com.ecommerce.auth.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu hiện tại không được trống")
    private String currentPassword;
    
    @NotBlank(message = "Mật khẩu mới không được trống")
    @Size(min = 6, max = 40, message = "Mật khẩu phải từ 6-40 ký tự")
    private String newPassword;
} 