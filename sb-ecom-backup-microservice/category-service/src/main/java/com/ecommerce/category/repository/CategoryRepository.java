package com.ecommerce.category.repository;

import com.ecommerce.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Category> findByIsActiveTrue();
    
    @Query("SELECT c FROM Category c WHERE c.featuredCategory = true AND c.isActive = true ORDER BY c.id")
    List<Category> findAllFeaturedCategories();
} 