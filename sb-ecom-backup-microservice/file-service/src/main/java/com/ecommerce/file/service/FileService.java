package com.ecommerce.file.service;

import com.ecommerce.file.model.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface FileService {
    
    /**
     * Store a file and return file information
     * 
     * @param file The file to store
     * @return Information about the stored file
     * @throws IOException If an I/O error occurs
     */
    FileInfo store(MultipartFile file) throws IOException;
    
    /**
     * Get a file by its ID
     * 
     * @param id The ID of the file
     * @return The file information if found
     */
    Optional<FileInfo> getFile(Long id);
    
    /**
     * Get a file by its name or filename
     * 
     * @param filename The name or part of the filename to search for
     * @return The file information if found
     */
    Optional<FileInfo> getFileByName(String filename);
    
    /**
     * Get all files
     * 
     * @return List of all files
     */
    List<FileInfo> getAllFiles();
    
    /**
     * Delete a file by its ID
     * 
     * @param id The ID of the file to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteFile(Long id);

    /**
     * Store multiple files and return file information for each
     * 
     * @param files Array of files to store
     * @return List of information about each stored file
     * @throws IOException If an I/O error occurs
     */
    List<FileInfo> storeMultiple(MultipartFile[] files) throws IOException;
}