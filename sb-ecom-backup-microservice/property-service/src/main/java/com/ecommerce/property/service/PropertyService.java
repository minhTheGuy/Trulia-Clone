package com.ecommerce.property.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.property.payload.dto.PropertyDTO;
import com.ecommerce.property.payload.response.PropertyResponse;

public interface PropertyService {
    PropertyDTO addProperty(Long categoryId, PropertyDTO property);

    PropertyResponse getAllProperties(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, String category);

    PropertyResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    PropertyResponse searchPropertyByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    
    PropertyResponse searchPropertyByFilters(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,
                                           Double minPrice, Double maxPrice, Integer minBedrooms, Integer minBathrooms,
                                           Double minSquareFootage, String propertyType, Boolean forSale, Boolean forRent);

    PropertyDTO updateProperty(Long propertyId, PropertyDTO property);

    PropertyDTO deleteProperty(Long propertyId);

    PropertyDTO updatePropertyImages(Long propertyId, MultipartFile[] images) throws IOException;
    
    PropertyResponse getPropertiesByLocation(String city, String state, String zipcode, 
                                           Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
                                           
    List<PropertyDTO> getPropertiesBySellerId(Long userId);

    PropertyDTO getPropertyById(Long id);

    PropertyDTO incrementPropertyViews(Long id);
}