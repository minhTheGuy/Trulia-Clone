package com.ecommerce.property.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.property.model.Rental;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);
    List<Rental> findByPropertyId(Long propertyId);
}