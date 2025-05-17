package com.ecommerce.property.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.property.model.PropertyTour;
import com.ecommerce.property.model.PropertyTour.TourStatus;

@Repository
public interface PropertyTourRepository extends JpaRepository<PropertyTour, Long> {
    
    List<PropertyTour> findByUserId(Long userId);
    
    List<PropertyTour> findByPropertyId(Long propertyId);
    
    List<PropertyTour> findByUserIdAndStatus(Long userId, TourStatus status);
    
    List<PropertyTour> findByTourDateBetween(LocalDate start, LocalDate end);
    
    List<PropertyTour> findByPropertyIdAndTourDateBetween(Long propertyId, LocalDate start, LocalDate end);
} 