package com.ecommerce.property.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.property.model.Property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findByTitleContainingIgnoreCase(@NotBlank @Size(min = 3, message = "Property title must contain at least 3 characters") String title, Pageable pageable);
    
    @Query("SELECT p FROM Property p WHERE p.categoryId = :categoryId")
    Page<Property> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Property p WHERE (:keyword IS NULL OR p.title LIKE %:keyword% OR p.description LIKE %:keyword%) " +
           "AND (:category IS NULL OR CONCAT(p.categoryId, '') = :category)")
    Page<Property> searchProperties(
            @Param("keyword") String keyword,
            @Param("category") String category,
            Pageable pageable);
    
    @Query("SELECT p FROM Property p WHERE " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minBedrooms IS NULL OR p.bedrooms >= :minBedrooms) AND " +
           "(:minBathrooms IS NULL OR p.bathrooms >= :minBathrooms) AND " +
           "(:minSquareFootage IS NULL OR p.sqft >= :minSquareFootage) AND " +
           "(:propertyType IS NULL OR p.type = :propertyType) AND " +
           "(:forSale IS NULL OR p.forSale = :forSale) AND " +
           "(:forRent IS NULL OR p.forRent = :forRent)")
    Page<Property> searchByFilters(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minBedrooms") Integer minBedrooms,
            @Param("minBathrooms") Integer minBathrooms,
            @Param("minSquareFootage") Double minSquareFootage,
            @Param("propertyType") String propertyType,
            @Param("forSale") Boolean forSale,
            @Param("forRent") Boolean forRent,
            Pageable pageable);
    
    @Query("SELECT p FROM Property p WHERE " +
           "(:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(p.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:zipcode IS NULL OR p.zip = :zipcode)")
    Page<Property> findByLocation(
            @Param("city") String city,
            @Param("state") String state, 
            @Param("zipcode") String zipcode,
            Pageable pageable);

    List<Property> findByUserId(Long userId);
}