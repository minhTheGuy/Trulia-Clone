package com.ecommerce.file.repository;

import com.ecommerce.file.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileInfo, Long> {
    /**
     * Find a file by its name (containing the given string)
     * 
     * @param name Part of the filename to search for
     * @return The file information if found
     */
    Optional<FileInfo> findByNameContaining(String name);
    
    /**
     * Find a file by its original filename
     * 
     * @param originalFilename The original filename to search for
     * @return The file information if found
     */
    Optional<FileInfo> findByOriginalFilename(String originalFilename);
}