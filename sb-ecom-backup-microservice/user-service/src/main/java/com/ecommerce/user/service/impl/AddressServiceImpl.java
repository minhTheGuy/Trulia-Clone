package com.ecommerce.user.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.user.payload.dto.AddressDTO;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.Address;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.AddressService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AddressDTO createAddress(Long userId, AddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        
        // If this is the first address for the user, set it as default
        if (addressRepository.findByUserId(userId).isEmpty()) {
            address.setDefault(true);
        }
        
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO getAddressById(Long userId, Long addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddressesByUserId(Long userId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        
        // Update address properties
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setAddressType(addressDTO.getAddressType());
        
        // Save updated address
        Address updatedAddress = addressRepository.save(address);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        
        // If we're deleting the default address, try to set another one as default
        if (address.isDefault()) {
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            if (userAddresses.size() > 1) {
                userAddresses.stream()
                        .filter(a -> !a.getId().equals(addressId))
                        .findFirst()
                        .ifPresent(a -> {
                            a.setDefault(true);
                            addressRepository.save(a);
                        });
            }
        }
        
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressDTO setDefaultAddress(Long userId, Long addressId) {
        // Verify address exists for this user
        Address newDefaultAddress = findAddressByIdAndUserId(addressId, userId);
        
        // Find current default address if any
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(currentDefault -> {
                    currentDefault.setDefault(false);
                    addressRepository.save(currentDefault);
                });
        
        // Set the new default
        newDefaultAddress.setDefault(true);
        Address savedAddress = addressRepository.save(newDefaultAddress);
        
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO getDefaultAddress(Long userId) {
        Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found for user with id: " + userId));
        
        return modelMapper.map(defaultAddress, AddressDTO.class);
    }
    
    private Address findAddressByIdAndUserId(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + addressId + " for user with id: " + userId));
    }
} 