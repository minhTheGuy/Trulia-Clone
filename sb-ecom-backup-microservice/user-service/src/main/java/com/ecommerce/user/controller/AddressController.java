package com.ecommerce.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user.payload.dto.AddressDTO;
import com.ecommerce.user.service.AddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO createdAddress = addressService.createAddress(userId, addressDTO);
        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        AddressDTO address = addressService.getAddressById(userId, addressId);
        return ResponseEntity.ok(address);
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAddresses(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getAllAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO updatedAddress = addressService.updateAddress(userId, addressId, addressDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<AddressDTO> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        AddressDTO defaultAddress = addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(defaultAddress);
    }

    @GetMapping("/default")
    public ResponseEntity<AddressDTO> getDefaultAddress(@PathVariable Long userId) {
        AddressDTO defaultAddress = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(defaultAddress);
    }
} 