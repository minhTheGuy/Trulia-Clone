package com.ecommerce.file.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.file.exception.FileStorageException;
import com.ecommerce.file.model.FileInfo;
import com.ecommerce.file.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${file.upload.directory}")
    private String uploadDir;
    
    private final FileRepository fileRepository;

    @Override
    public FileInfo store(MultipartFile file) throws IOException {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file");
            }
            
            // Normalize file name
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            
            // Check for invalid characters in filename
            if (originalFilename.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
            }
            
            // Generate unique file name with UUID and preserve original extension
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Create storage directories by date to organize files
            LocalDateTime now = LocalDateTime.now();
            Path yearPath = uploadPath.resolve(String.valueOf(now.getYear()));
            Path monthPath = yearPath.resolve(String.format("%02d", now.getMonthValue()));
            
            if (!Files.exists(yearPath)) {
                Files.createDirectories(yearPath);
            }
            if (!Files.exists(monthPath)) {
                Files.createDirectories(monthPath);
            }
            
            // Copy file to the target location with appropriate path structure
            Path targetLocation = monthPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Store relative path to file from upload directory
            String relativePath = Paths.get(String.valueOf(now.getYear()))
                    .resolve(String.format("%02d", now.getMonthValue()))
                    .resolve(uniqueFilename).toString();
            
            // Create file info entity
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(relativePath);
            fileInfo.setOriginalFilename(originalFilename);
            fileInfo.setContentType(file.getContentType());
            fileInfo.setSize(file.getSize());
            fileInfo.setUrl("/api/files/view/" + uniqueFilename); // Will be updated by controller
            fileInfo.setUploadDate(now);
            
            // Save file info to database
            return fileRepository.save(fileInfo);
        } catch (IOException e) {
            logger.error("Failed to store file", e);
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }
    
    @Override
    public Optional<FileInfo> getFile(Long id) {
        return fileRepository.findById(id);
    }
    
    @Override
    public Optional<FileInfo> getFileByName(String filename) {
        // Search for file by name (useful for direct access by filename)
        return fileRepository.findByNameContaining(filename);
    }
    
    @Override
    public List<FileInfo> getAllFiles() {
        return fileRepository.findAll();
    }
    
    @Override
    public boolean deleteFile(Long id) {
        Optional<FileInfo> fileInfoOpt = fileRepository.findById(id);
        
        if (fileInfoOpt.isPresent()) {
            FileInfo fileInfo = fileInfoOpt.get();
            
            // Delete file from storage
            try {
                Path filePath = Paths.get(uploadDir).resolve(fileInfo.getName());
                Files.deleteIfExists(filePath);
                
                // Delete file info from database
                fileRepository.delete(fileInfo);
                return true;
            } catch (IOException e) {
                logger.error("Error deleting file: {}", e.getMessage());
                return false;
            }
        }
        
        return false;
    }

    @Override
    public List<FileInfo> storeMultiple(MultipartFile[] files) throws IOException {
        List<FileInfo> storedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            try {
                FileInfo fileInfo = store(file);
                storedFiles.add(fileInfo);
            } catch (IOException e) {
                logger.error("Error storing file: {}", e.getMessage());
                // Continue with other files even if one fails
            }
        }
        
        return storedFiles;
    }
}