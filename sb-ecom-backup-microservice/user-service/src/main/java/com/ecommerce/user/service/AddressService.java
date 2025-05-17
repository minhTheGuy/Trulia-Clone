package com.ecommerce.user.service;

import java.util.List;

import com.ecommerce.user.payload.dto.AddressDTO;

public interface AddressService {
    
    AddressDTO createAddress(Long userId, AddressDTO addressDTO);
    
    AddressDTO getAddressById(Long userId, Long addressId);
    
    List<AddressDTO> getAllAddressesByUserId(Long userId);
    
    AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO);
    
    void deleteAddress(Long userId, Long addressId);
    
    AddressDTO setDefaultAddress(Long userId, Long addressId);
    
    AddressDTO getDefaultAddress(Long userId);
} 