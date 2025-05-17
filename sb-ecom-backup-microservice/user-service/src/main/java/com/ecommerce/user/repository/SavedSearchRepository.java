package com.ecommerce.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.user.model.SavedSearch;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    List<SavedSearch> findByUserId(Long userId);
    void deleteByUserIdAndId(Long userId, Long id);
} 