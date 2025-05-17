package com.ecommerce.category.service;

import java.util.List;

import com.ecommerce.category.payload.dto.CategoryDTO;

public interface CategoryService {
    
    // Standard CRUD operations
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    
    void deleteCategory(Long id);
    
    CategoryDTO getCategoryById(Long id);
    
    List<CategoryDTO> getAllCategories();
    
    List<CategoryDTO> getAllActiveCategories();
    
    boolean isCategoryExists(String name);
    
    // Featured categories
    List<CategoryDTO> getAllFeaturedCategories();
} 