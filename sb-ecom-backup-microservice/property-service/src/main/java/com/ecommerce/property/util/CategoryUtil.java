package com.ecommerce.property.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.property.model.PropertyCategory;
import com.ecommerce.property.payload.dto.CategoryDTO;

/**
 * Lớp tiện ích cung cấp các phương thức làm việc với categories
 */
public class CategoryUtil {

    /**
     * Lấy danh sách tất cả categories dưới dạng CategoryDTO
     * @return Danh sách CategoryDTO
     */
    public static List<CategoryDTO> getAllCategories() {
        return Arrays.stream(PropertyCategory.values())
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy category theo ID
     * @param categoryId ID của category cần tìm
     * @return CategoryDTO tương ứng hoặc null nếu không tìm thấy
     */
    public static CategoryDTO getCategoryById(Long categoryId) {
        PropertyCategory category = PropertyCategory.fromId(categoryId);
        if (category == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
    
    /**
     * Kiểm tra xem một categoryId có tồn tại không
     * @param categoryId ID cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     */
    public static boolean isValidCategoryId(Long categoryId) {
        return PropertyCategory.fromId(categoryId) != null;
    }
} 