package com.ecommerce.property.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.property.exceptions.APIException;
import com.ecommerce.property.payload.dto.CategoryDTO;
import com.ecommerce.property.util.CategoryUtil;

/**
 * Controller cung cấp API cho các categories bất động sản
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    /**
     * Lấy danh sách tất cả các categories
     * @return Danh sách CategoryDTO
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = CategoryUtil.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
    
    /**
     * Lấy category theo ID
     * @param categoryId ID của category cần tìm
     * @return CategoryDTO tương ứng
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long categoryId) {
        CategoryDTO category = CategoryUtil.getCategoryById(categoryId);
        if (category == null) {
            throw new APIException("Category not found with id: " + categoryId);
        }
        return new ResponseEntity<>(category, HttpStatus.OK);
    }
} 