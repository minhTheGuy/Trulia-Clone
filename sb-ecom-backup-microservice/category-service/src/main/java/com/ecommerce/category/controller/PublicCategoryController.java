package com.ecommerce.category.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.category.payload.dto.CategoryDTO;
import com.ecommerce.category.service.CategoryService;

import lombok.RequiredArgsConstructor;

/**
 * Controller for public (non-authenticated) endpoints for categories
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories/public")
public class PublicCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(PublicCategoryController.class);
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllPublicCategories() {
        logger.info("Public request for all active categories");
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getPublicCategoryById(@PathVariable Long id) {
        logger.info("Public request for category with ID: {}", id);
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<CategoryDTO>> getPublicFeaturedCategories() {
        logger.info("Public request for featured categories");
        return ResponseEntity.ok(categoryService.getAllFeaturedCategories());
    }
    
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkPublicCategoryExists(@RequestParam String name) {
        logger.info("Public request to check if category exists: {}", name);
        return ResponseEntity.ok(categoryService.isCategoryExists(name));
    }
} 