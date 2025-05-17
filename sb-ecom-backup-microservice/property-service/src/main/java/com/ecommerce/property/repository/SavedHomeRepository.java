package com.ecommerce.property.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.property.model.SavedHome;

@Repository
public interface SavedHomeRepository extends JpaRepository<SavedHome, Long> {
    
    List<SavedHome> findByUserId(Long userId);
    
    Optional<SavedHome> findByUserIdAndPropertyId(Long userId, Long propertyId);
    
    void deleteByUserIdAndPropertyId(Long userId, Long propertyId);
    
    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);
} 