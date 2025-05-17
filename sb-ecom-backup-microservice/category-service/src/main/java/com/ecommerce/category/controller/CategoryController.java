package com.ecommerce.category.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.category.payload.dto.CategoryDTO;
import com.ecommerce.category.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for authenticated/protected category endpoints
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    // Standard CRUD endpoints
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        logger.info("Creating new category: {}", categoryDTO.getName());
        
        try {
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handle duplicate category name
            logger.warn("Failed to create category: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating category: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        logger.info("Updating category with ID: {}", id);
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("Deleting category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        logger.info("Getting category with ID: {}", id);
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        logger.info("Getting all categories");
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryDTO>> getAllActiveCategories() {
        logger.info("Getting all active categories");
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }
    
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkCategoryExists(@RequestParam String name) {
        logger.info("Checking if category exists: {}", name);
        return ResponseEntity.ok(categoryService.isCategoryExists(name));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<CategoryDTO>> getFeaturedCategories() {
        logger.info("Getting featured categories");
        return ResponseEntity.ok(categoryService.getAllFeaturedCategories());
    }
} 