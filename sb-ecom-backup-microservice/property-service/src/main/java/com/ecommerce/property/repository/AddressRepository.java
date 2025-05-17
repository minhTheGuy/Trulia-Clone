package com.ecommerce.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.property.model.Address;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
