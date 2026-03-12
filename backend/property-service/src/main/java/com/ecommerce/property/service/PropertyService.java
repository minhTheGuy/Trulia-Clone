package com.ecommerce.property.service;

import java.io.IOException;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.property.payload.dto.PropertyDTO;
import com.ecommerce.property.payload.request.PropertyRequest;
import com.ecommerce.property.payload.response.PagedResponse;
import com.ecommerce.property.payload.response.PropertyResponse;

public interface PropertyService {
    PropertyResponse getAllProperties(Integer pageNumber, Integer pageSize, String sortBy, String sortDir, String status, String category);
    
    PagedResponse<PropertyDTO> getAllProperties(int pageNumber, int pageSize, String sortBy, String sortDir);
    
    PropertyDTO getPropertyById(Long id);

    PropertyDTO updateProperty(Long id, @Valid PropertyDTO propertyRequest);
    
    PropertyDTO deleteProperty(Long id);
    
    PagedResponse<PropertyDTO> searchPropertiesByKeyword(String keyword, int pageNumber, int pageSize, String sortBy, String sortDir);
    
    PagedResponse<PropertyDTO> getPropertiesByCategory(Long categoryId, int pageNumber, int pageSize, String sortBy, String sortDir);
    
    PagedResponse<PropertyDTO> getPropertiesBySellerId(Long sellerId, int pageNumber, int pageSize, String sortBy, String sortDir);
    
    Long countPropertiesBySellerId(Long sellerId);

    PropertyDTO addProperty(Long categoryId, PropertyDTO property);

    PropertyResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    PropertyResponse searchPropertyByFilters(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,
                                           Double minPrice, Double maxPrice, Integer minBedrooms, Integer minBathrooms,
                                           Double minSquareFootage, String propertyType, Boolean forSale, Boolean forRent);

    PropertyDTO updatePropertyImages(Long propertyId, MultipartFile[] images) throws IOException;
    
    PropertyResponse getPropertiesByLocation(String city, String state, String zipcode, 
                                           Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
                                           
    List<PropertyDTO> getPropertiesBySellerId(Long userId);

    PropertyDTO incrementPropertyViews(Long id);

    PropertyResponse searchPropertyByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}