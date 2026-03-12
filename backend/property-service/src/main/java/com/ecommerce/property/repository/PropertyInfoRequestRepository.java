package com.ecommerce.property.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.property.model.PropertyInfoRequest;
import com.ecommerce.property.model.PropertyInfoRequest.RequestStatus;

@Repository
public interface PropertyInfoRequestRepository extends JpaRepository<PropertyInfoRequest, Long> {
    
    List<PropertyInfoRequest> findByUserId(Long userId);
    
    List<PropertyInfoRequest> findBySellerUserId(Long sellerUserId);
    
    List<PropertyInfoRequest> findByPropertyId(Long propertyId);
    
    List<PropertyInfoRequest> findByUserIdAndStatus(Long userId, RequestStatus status);
    
    List<PropertyInfoRequest> findBySellerUserIdAndStatus(Long sellerUserId, RequestStatus status);
    
    List<PropertyInfoRequest> findByEmail(String email);
    
    List<PropertyInfoRequest> findByPhone(String phone);
} 