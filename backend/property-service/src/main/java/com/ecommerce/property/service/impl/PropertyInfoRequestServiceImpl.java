package com.ecommerce.property.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.ecommerce.property.exceptions.ResourceNotFoundException;
import com.ecommerce.property.model.Property;
import com.ecommerce.property.model.PropertyInfoRequest;
import com.ecommerce.property.model.PropertyInfoRequest.ContactPreference;
import com.ecommerce.property.model.PropertyInfoRequest.RequestStatus;
import com.ecommerce.property.payload.dto.PropertyInfoRequestDTO;
import com.ecommerce.property.repository.PropertyInfoRequestRepository;
import com.ecommerce.property.repository.PropertyRepository;
import com.ecommerce.property.service.PropertyInfoRequestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyInfoRequestServiceImpl implements PropertyInfoRequestService {

    private final PropertyInfoRequestRepository infoRequestRepository;
    private final PropertyRepository propertyRepository;
    private final ModelMapper modelMapper;

    @Override
    public PropertyInfoRequestDTO createInfoRequest(PropertyInfoRequestDTO requestDTO) {
        // Validate property exists
        Property property = propertyRepository.findById(requestDTO.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + requestDTO.getPropertyId()));
        
        // Convert DTO to entity
        PropertyInfoRequest request = modelMapper.map(requestDTO, PropertyInfoRequest.class);
        
        // Set default values if not provided
        if (request.getPreferredContact() == null) {
            request.setPreferredContact(ContactPreference.EMAIL);
        }
        
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        // Save request
        PropertyInfoRequest savedRequest = infoRequestRepository.save(request);
        
        // Convert back to DTO
        PropertyInfoRequestDTO savedRequestDTO = modelMapper.map(savedRequest, PropertyInfoRequestDTO.class);
        
        // Enrich DTO with property details
        if (property.getTitle() != null) {
            savedRequestDTO.setPropertyTitle(property.getTitle());
        }
        savedRequestDTO.setPropertyAddress(property.getAddress());
        if (property.getImages() != null && !property.getImages().isEmpty()) {
            savedRequestDTO.setPropertyImage(property.getImages().get(0));
        }
        
        return savedRequestDTO;
    }

    @Override
    public PropertyInfoRequestDTO getInfoRequestById(Long id) {
        PropertyInfoRequest request = infoRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Information request not found with id: " + id));
        
        PropertyInfoRequestDTO requestDTO = modelMapper.map(request, PropertyInfoRequestDTO.class);
        enrichRequestDTO(requestDTO);
        
        return requestDTO;
    }

    @Override
    public List<PropertyInfoRequestDTO> getInfoRequestsByUserId(Long userId) {
        List<PropertyInfoRequest> requests = infoRequestRepository.findByUserId(userId);
        return requests.stream()
                .map(request -> {
                    PropertyInfoRequestDTO dto = modelMapper.map(request, PropertyInfoRequestDTO.class);
                    enrichRequestDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyInfoRequestDTO> getInfoRequestsBySellerUserId(Long sellerUserId) {
        List<PropertyInfoRequest> requests = infoRequestRepository.findBySellerUserId(sellerUserId);
        return requests.stream()
                .map(request -> {
                    PropertyInfoRequestDTO dto = modelMapper.map(request, PropertyInfoRequestDTO.class);
                    enrichRequestDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyInfoRequestDTO> getInfoRequestsByPropertyId(Long propertyId) {
        List<PropertyInfoRequest> requests = infoRequestRepository.findByPropertyId(propertyId);
        return requests.stream()
                .map(request -> {
                    PropertyInfoRequestDTO dto = modelMapper.map(request, PropertyInfoRequestDTO.class);
                    enrichRequestDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyInfoRequestDTO> getInfoRequestsByUserIdAndStatus(Long userId, RequestStatus status) {
        List<PropertyInfoRequest> requests = infoRequestRepository.findByUserIdAndStatus(userId, status);
        return requests.stream()
                .map(request -> {
                    PropertyInfoRequestDTO dto = modelMapper.map(request, PropertyInfoRequestDTO.class);
                    enrichRequestDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyInfoRequestDTO> getInfoRequestsBySellerUserIdAndStatus(Long sellerUserId, RequestStatus status) {
        List<PropertyInfoRequest> requests = infoRequestRepository.findBySellerUserIdAndStatus(sellerUserId, status);
        return requests.stream()
                .map(request -> {
                    PropertyInfoRequestDTO dto = modelMapper.map(request, PropertyInfoRequestDTO.class);
                    enrichRequestDTO(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PropertyInfoRequestDTO updateRequestStatus(Long id, RequestStatus status) {
        PropertyInfoRequest request = infoRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Information request not found with id: " + id));
        
        request.setStatus(status);
        request.setUpdatedAt(LocalDateTime.now());
        
        PropertyInfoRequest updatedRequest = infoRequestRepository.save(request);
        
        PropertyInfoRequestDTO requestDTO = modelMapper.map(updatedRequest, PropertyInfoRequestDTO.class);
        enrichRequestDTO(requestDTO);
        
        return requestDTO;
    }

    @Override
    public PropertyInfoRequestDTO respondToRequest(Long id, String response) {
        PropertyInfoRequest request = infoRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Information request not found with id: " + id));
        
        // In a real application, we might store the response in another field or table
        // Here we will just update the status
        request.setStatus(RequestStatus.RESPONDED);
        request.setUpdatedAt(LocalDateTime.now());
        
        PropertyInfoRequest updatedRequest = infoRequestRepository.save(request);
        
        PropertyInfoRequestDTO requestDTO = modelMapper.map(updatedRequest, PropertyInfoRequestDTO.class);
        enrichRequestDTO(requestDTO);
        
        return requestDTO;
    }

    @Override
    public void deleteInfoRequest(Long id) {
        PropertyInfoRequest request = infoRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Information request not found with id: " + id));
        
        infoRequestRepository.delete(request);
    }
    
    /**
     * Helper method to enrich request DTOs with related data
     */
    private void enrichRequestDTO(PropertyInfoRequestDTO requestDTO) {
        try {
            Property property = propertyRepository.findById(requestDTO.getPropertyId())
                    .orElse(null);
            
            if (property != null) {
                requestDTO.setPropertyTitle(property.getTitle());
                requestDTO.setPropertyAddress(property.getAddress());
                if (property.getImages() != null && !property.getImages().isEmpty()) {
                    requestDTO.setPropertyImage(property.getImages().get(0));
                }
                
                // Set seller information from property info
                requestDTO.setSellerName(property.getAgentName());
                requestDTO.setSellerEmail(property.getAgentEmail());
                requestDTO.setSellerPhone(property.getAgentPhone());
            }
            
            // Note: In a real app, you would fetch and set seller details from a user service
        } catch (Exception e) {
            // Log but don't fail if enrichment fails
        }
    }
} 