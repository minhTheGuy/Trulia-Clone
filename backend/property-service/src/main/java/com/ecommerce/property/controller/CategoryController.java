package com.ecommerce.property.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.property.model.PropertyCategory;
import com.ecommerce.property.payload.dto.CategoryDTO;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @GetMapping("/public")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = Arrays.stream(PropertyCategory.values())
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        PropertyCategory category = PropertyCategory.fromId(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new CategoryDTO(category.getId(), category.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesProtected() {
        return getAllCategories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryByIdProtected(@PathVariable Long id) {
        return getCategoryById(id);
    }
}
