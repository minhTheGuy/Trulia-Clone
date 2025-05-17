package com.ecommerce.file.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecommerce.file.exception.FileStorageException;
import com.ecommerce.file.model.FileInfo;
import com.ecommerce.file.model.FileResponse;
import com.ecommerce.file.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@CrossOrigin(origins = "${frontend.url}")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    @Value("${file.upload.directory}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    FileResponse.builder()
                        .success(false)
                        .message("Failed to upload empty file")
                        .build()
                );
            }
            
            FileInfo fileInfo = fileService.store(file);
            
            // Create URLs for the file
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(fileInfo.getId().toString())
                .toUriString();
                
            String fileViewUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/view/")
                .path(fileInfo.getId().toString())
                .toUriString();
            
            return ResponseEntity.ok(
                FileResponse.builder()
                    .id(fileInfo.getId())
                    .name(fileInfo.getName())
                    .originalFilename(fileInfo.getOriginalFilename())
                    .contentType(fileInfo.getContentType())
                    .size(fileInfo.getSize())
                    .downloadUrl(fileDownloadUri)
                    .viewUrl(fileViewUri)
                    .url(fileViewUri) // backwards compatibility
                    .uploadDate(fileInfo.getUploadDate())
                    .success(true)
                    .message("File uploaded successfully")
                    .build()
            );
        } catch (FileStorageException | IOException e) {
            logger.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                FileResponse.builder()
                    .success(false)
                    .message("Failed to upload file: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @PostMapping("/upload/multiple")
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files.length == 0) {
                return ResponseEntity.badRequest().body(
                    FileResponse.builder()
                        .success(false)
                        .message("No files provided for upload")
                        .build()
                );
            }
            
            List<FileInfo> fileInfos = fileService.storeMultiple(files);
            
            if (fileInfos.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    FileResponse.builder()
                        .success(false)
                        .message("No valid files were uploaded")
                        .build()
                );
            }
            
            // Create response objects with URLs
            List<FileResponse> responses = fileInfos.stream()
                .map(fileInfo -> {
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/files/download/")
                        .path(fileInfo.getId().toString())
                        .toUriString();
                        
                    String fileViewUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/files/view/")
                        .path(fileInfo.getId().toString())
                        .toUriString();
                    
                    return FileResponse.builder()
                        .id(fileInfo.getId())
                        .name(fileInfo.getName())
                        .originalFilename(fileInfo.getOriginalFilename())
                        .contentType(fileInfo.getContentType())
                        .size(fileInfo.getSize())
                        .downloadUrl(fileDownloadUri)
                        .viewUrl(fileViewUri)
                        .url(fileViewUri) // backwards compatibility
                        .uploadDate(fileInfo.getUploadDate())
                        .success(true)
                        .build();
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            logger.error("Error uploading multiple files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                FileResponse.builder()
                    .success(false)
                    .message("Failed to upload files: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllFiles() {
        try {
            List<FileInfo> files = fileService.getAllFiles();
            
            List<FileResponse> responses = files.stream()
                .map(fileInfo -> {
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/files/download/")
                        .path(fileInfo.getId().toString())
                        .toUriString();
                        
                    String fileViewUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/files/view/")
                        .path(fileInfo.getId().toString())
                        .toUriString();
                    
                    return FileResponse.builder()
                        .id(fileInfo.getId())
                        .name(fileInfo.getName())
                        .originalFilename(fileInfo.getOriginalFilename())
                        .contentType(fileInfo.getContentType())
                        .size(fileInfo.getSize())
                        .downloadUrl(fileDownloadUri)
                        .viewUrl(fileViewUri)
                        .url(fileViewUri)
                        .uploadDate(fileInfo.getUploadDate())
                        .success(true)
                        .build();
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error getting all files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                FileResponse.builder()
                    .success(false)
                    .message("Failed to get files: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getFileInfo(@PathVariable Long id) {
        try {
            Optional<FileInfo> fileInfoOpt = fileService.getFile(id);
            
            if (fileInfoOpt.isPresent()) {
                FileInfo fileInfo = fileInfoOpt.get();
                
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(fileInfo.getId().toString())
                    .toUriString();
                    
                String fileViewUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/view/")
                    .path(fileInfo.getId().toString())
                    .toUriString();
                
                FileResponse response = FileResponse.builder()
                    .id(fileInfo.getId())
                    .name(fileInfo.getName())
                    .originalFilename(fileInfo.getOriginalFilename())
                    .contentType(fileInfo.getContentType())
                    .size(fileInfo.getSize())
                    .downloadUrl(fileDownloadUri)
                    .viewUrl(fileViewUri)
                    .url(fileViewUri)
                    .uploadDate(fileInfo.getUploadDate())
                    .success(true)
                    .build();
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting file info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                FileResponse.builder()
                    .success(false)
                    .message("Failed to get file info: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Optional<FileInfo> fileInfoOpt = fileService.getFile(id);
        
        if (fileInfoOpt.isPresent()) {
            FileInfo fileInfo = fileInfoOpt.get();
            try {
                Path filePath = Paths.get(uploadDir).resolve(fileInfo.getName());
                Resource resource = new UrlResource(filePath.toUri());
                
                if (resource.exists()) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                            .body(resource);
                } else {
                    logger.error("File not found on disk: {}", filePath);
                    return ResponseEntity.notFound().build();
                }
            } catch (MalformedURLException e) {
                logger.error("Error downloading file", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long id) {
        Optional<FileInfo> fileInfoOpt = fileService.getFile(id);
        
        if (fileInfoOpt.isPresent()) {
            FileInfo fileInfo = fileInfoOpt.get();
            try {
                Path filePath = Paths.get(uploadDir).resolve(fileInfo.getName());
                Resource resource = new UrlResource(filePath.toUri());
                
                if (resource.exists()) {
                    // Return inline content for browser viewing instead of downloading
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                            .body(resource);
                } else {
                    logger.error("File not found on disk: {}", filePath);
                    return ResponseEntity.notFound().build();
                }
            } catch (MalformedURLException e) {
                logger.error("Error viewing file", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/by-name/{filename}")
    public ResponseEntity<?> getFileByName(@PathVariable String filename) {
        try {
            Optional<FileInfo> fileInfoOpt = fileService.getFileByName(filename);
            
            if (fileInfoOpt.isPresent()) {
                FileInfo fileInfo = fileInfoOpt.get();
                
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(fileInfo.getId().toString())
                    .toUriString();
                    
                String fileViewUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/view/")
                    .path(fileInfo.getId().toString())
                    .toUriString();
                
                FileResponse response = FileResponse.builder()
                    .id(fileInfo.getId())
                    .name(fileInfo.getName())
                    .originalFilename(fileInfo.getOriginalFilename())
                    .contentType(fileInfo.getContentType())
                    .size(fileInfo.getSize())
                    .downloadUrl(fileDownloadUri)
                    .viewUrl(fileViewUri)
                    .url(fileViewUri)
                    .uploadDate(fileInfo.getUploadDate())
                    .success(true)
                    .build();
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting file by name", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                FileResponse.builder()
                    .success(false)
                    .message("Failed to get file by name: " + e.getMessage())
                    .build()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            boolean deleted = fileService.deleteFile(id);
            
            if (deleted) {
                return ResponseEntity.ok(
                    FileResponse.builder()
                        .id(id)
                        .success(true)
                        .message("File deleted successfully")
                        .build()
                );
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error deleting file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                FileResponse.builder()
                    .success(false)
                    .message("Failed to delete file: " + e.getMessage())
                    .build()
            );
        }
    }
    
    // API for property-service integration
    @PostMapping("/property/images")
    public ResponseEntity<?> uploadPropertyImages(@RequestParam("files") MultipartFile[] files, 
                                               @RequestParam(value = "propertyId", required = false) Long propertyId) {
        try {
            if (files.length == 0) {
                return ResponseEntity.badRequest().body(
                    FileResponse.builder()
                        .success(false)
                        .message("No files provided for upload")
                        .build()
                );
            }
            
            // Create a subdirectory for property images if it doesn't exist
            Path propertyImagesPath = Paths.get(uploadDir, "properties");
            if (!Files.exists(propertyImagesPath)) {
                Files.createDirectories(propertyImagesPath);
            }
            
            List<FileInfo> fileInfos = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty() || !isImageFile(file)) {
                    continue;
                }
                
                FileInfo fileInfo = fileService.store(file);
                fileInfos.add(fileInfo);
            }
            
            if (fileInfos.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    FileResponse.builder()
                        .success(false)
                        .message("No valid image files were uploaded")
                        .build()
                );
            }
            
            // Create response objects with URLs
            List<FileResponse> responses = fileInfos.stream()
                .map(fileInfo -> {
                    String fileViewUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/files/view/")
                        .path(fileInfo.getId().toString())
                        .toUriString();
                    
                    return FileResponse.builder()
                        .id(fileInfo.getId())
                        .name(fileInfo.getName())
                        .originalFilename(fileInfo.getOriginalFilename())
                        .url(fileViewUri) // This is the URL that property-service will use
                        .success(true)
                        .build();
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            logger.error("Error uploading property images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                FileResponse.builder()
                    .success(false)
                    .message("Failed to upload property images: " + e.getMessage())
                    .build()
            );
        }
    }
    
    // Helper method to check if a file is an image
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}