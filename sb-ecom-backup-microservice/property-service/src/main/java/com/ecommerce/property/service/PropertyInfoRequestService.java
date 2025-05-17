package com.ecommerce.property.service;

import java.util.List;

import com.ecommerce.property.model.PropertyInfoRequest.RequestStatus;
import com.ecommerce.property.payload.dto.PropertyInfoRequestDTO;

public interface PropertyInfoRequestService {
    
    PropertyInfoRequestDTO createInfoRequest(PropertyInfoRequestDTO requestDTO);
    
    PropertyInfoRequestDTO getInfoRequestById(Long id);
    
    List<PropertyInfoRequestDTO> getInfoRequestsByUserId(Long userId);
    
    List<PropertyInfoRequestDTO> getInfoRequestsBySellerUserId(Long sellerUserId);
    
    List<PropertyInfoRequestDTO> getInfoRequestsByPropertyId(Long propertyId);
    
    List<PropertyInfoRequestDTO> getInfoRequestsByUserIdAndStatus(Long userId, RequestStatus status);
    
    List<PropertyInfoRequestDTO> getInfoRequestsBySellerUserIdAndStatus(Long sellerUserId, RequestStatus status);
    
    PropertyInfoRequestDTO updateRequestStatus(Long id, RequestStatus status);
    
    PropertyInfoRequestDTO respondToRequest(Long id, String response);
    
    void deleteInfoRequest(Long id);
} 