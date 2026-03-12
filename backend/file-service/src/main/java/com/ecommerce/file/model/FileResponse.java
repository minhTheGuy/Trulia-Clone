package com.ecommerce.file.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String name;
    private String originalFilename;
    private String url;
    private String downloadUrl;
    private String viewUrl;
    private String contentType;
    private long size;
    private LocalDateTime uploadDate;
    private boolean success;
    private String message;
} 