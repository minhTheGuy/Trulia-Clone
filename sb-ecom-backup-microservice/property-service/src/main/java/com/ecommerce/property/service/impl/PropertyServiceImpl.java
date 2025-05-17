package com.ecommerce.property.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ecommerce.property.service.PropertyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

import com.ecommerce.property.exceptions.APIException;
import com.ecommerce.property.exceptions.ResourceNotFoundException;
import com.ecommerce.property.model.Property;
import com.ecommerce.property.payload.dto.AddressDetailsDTO;
import com.ecommerce.property.payload.dto.AgentDTO;
import com.ecommerce.property.payload.dto.CategoryDTO;
import com.ecommerce.property.payload.dto.LocationDTO;
import com.ecommerce.property.payload.dto.NeighborhoodDTO;
import com.ecommerce.property.payload.dto.PropertyDTO;
import com.ecommerce.property.payload.response.PropertyResponse;
import com.ecommerce.property.repository.PropertyRepository;
import com.ecommerce.property.util.CategoryUtil;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    
    private final ModelMapper modelMapper;
    
    private final WebClient.Builder webClientBuilder;
    
    private final RestTemplate restTemplate;
    
    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);
    
    @Value("${project.image}")
    private String path;
    
    @Value("${image.base.url}")
    private String imageBaseUrl;

    @Override
    public PropertyDTO addProperty(Long categoryId, PropertyDTO propertyDTO) {
        // Check if the property already exists
        boolean isPropertyNotPresent = true; // This should use repository check

        // Validate category exists
        CategoryDTO category = CategoryUtil.getCategoryById(categoryId);
        
        if (category == null) {
            throw new APIException("Category not found with id: " + categoryId);
        }

        if (isPropertyNotPresent) {
            // Set the categoryId
            propertyDTO.setCategoryId(categoryId);
            propertyDTO.setCategoryName(category.getName());
            
            // Use our mapper to convert DTO to entity
            Property property = mapToProperty(propertyDTO);
            
            // Set default image if none provided
            if (property.getImages() == null || property.getImages().isEmpty()) {
                List<String> defaultImages = new ArrayList<>();
                defaultImages.add("default-property.jpg");
                property.setImages(defaultImages);
            }

            // Set default values for status if not provided
            if (property.getStatus() == null || property.getStatus().isEmpty()) {
                if (property.isForSale()) {
                    property.setStatus("for_sale");
                } else if (property.isForRent()) {
                    property.setStatus("for_rent");
                } else {
                    property.setStatus("available");
                }
            }

            // Save property
            Property savedProperty = propertyRepository.save(property);

            // Convert back to DTO with our mapper
            return mapToPropertyDTO(savedProperty);
        } else {
            throw new APIException("Property with title '" + propertyDTO.getTitle() + "' already exists in this category!");
        }
    }

    @Override
    public PropertyResponse getAllProperties(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, String category) {
        // Create Pageable with sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Execute search
        Page<Property> pageProperties = propertyRepository.searchProperties(keyword, category, pageDetails);
        List<Property> properties = pageProperties.getContent();

        // Map results to DTOs with our mapper function
        List<PropertyDTO> propertyDTOs = properties.stream()
                .map(this::mapToPropertyDTO)
                .collect(Collectors.toList());

        // Create response
        PropertyResponse propertyResponse = new PropertyResponse();
        propertyResponse.setContent(propertyDTOs);
        propertyResponse.setPageNumber(pageProperties.getNumber());
        propertyResponse.setPageSize(pageProperties.getSize());
        propertyResponse.setTotalElements(pageProperties.getTotalElements());
        propertyResponse.setTotalPages(pageProperties.getTotalPages());
        propertyResponse.setLastPage(pageProperties.isLast());

        return propertyResponse;
    }

    @Override
    public PropertyResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Validate category exists
        CategoryDTO category = CategoryUtil.getCategoryById(categoryId);
        
        if (category == null) {
            throw new APIException("Category not found with id: " + categoryId);
        }

        // Create Pageable with sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Search properties by category
        Page<Property> pageProperties = propertyRepository.findByCategoryId(categoryId, pageDetails);
        List<Property> properties = pageProperties.getContent();

        // Map results to DTOs with our mapper function
        List<PropertyDTO> propertyDTOs = properties.stream()
                .map(property -> {
                    PropertyDTO dto = mapToPropertyDTO(property);
                    dto.setCategoryName(category.getName());
                    return dto;
                })
                .collect(Collectors.toList());

        // Create response
        PropertyResponse propertyResponse = new PropertyResponse();
        propertyResponse.setContent(propertyDTOs);
        propertyResponse.setPageNumber(pageProperties.getNumber());
        propertyResponse.setPageSize(pageProperties.getSize());
        propertyResponse.setTotalElements(pageProperties.getTotalElements());
        propertyResponse.setTotalPages(pageProperties.getTotalPages());
        propertyResponse.setLastPage(pageProperties.isLast());

        return propertyResponse;
    }

    @Override
    public PropertyResponse searchPropertyByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Create Pageable with sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Search by keyword
        Page<Property> pageProperties = propertyRepository.findByTitleContainingIgnoreCase(keyword, pageDetails);
        List<Property> properties = pageProperties.getContent();

        // Map results to DTOs with our mapper function
        List<PropertyDTO> propertyDTOs = properties.stream()
                .map(this::mapToPropertyDTO)
                .collect(Collectors.toList());

        // Create response
        PropertyResponse propertyResponse = new PropertyResponse();
        propertyResponse.setContent(propertyDTOs);
        propertyResponse.setPageNumber(pageProperties.getNumber());
        propertyResponse.setPageSize(pageProperties.getSize());
        propertyResponse.setTotalElements(pageProperties.getTotalElements());
        propertyResponse.setTotalPages(pageProperties.getTotalPages());
        propertyResponse.setLastPage(pageProperties.isLast());

        return propertyResponse;
    }

    @Override
    public PropertyResponse searchPropertyByFilters(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,
                                                   Double minPrice, Double maxPrice, Integer minBedrooms, Integer minBathrooms,
                                                   Double minSquareFootage, String propertyType, Boolean forSale, Boolean forRent) {
        // Create Pageable with sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Execute query with filters
        Page<Property> pageProperties = propertyRepository.searchByFilters(
                minPrice, maxPrice, minBedrooms, minBathrooms, minSquareFootage,
                propertyType, forSale, forRent, pageDetails);

        List<Property> properties = pageProperties.getContent();

        // Map results to DTOs with our mapper function
        List<PropertyDTO> propertyDTOs = properties.stream()
                .map(this::mapToPropertyDTO)
                .collect(Collectors.toList());

        // Create response
        PropertyResponse propertyResponse = new PropertyResponse();
        propertyResponse.setContent(propertyDTOs);
        propertyResponse.setPageNumber(pageProperties.getNumber());
        propertyResponse.setPageSize(pageProperties.getSize());
        propertyResponse.setTotalElements(pageProperties.getTotalElements());
        propertyResponse.setTotalPages(pageProperties.getTotalPages());
        propertyResponse.setLastPage(pageProperties.isLast());

        return propertyResponse;
    }

    @Override
    public PropertyDTO updateProperty(Long propertyId, PropertyDTO propertyDTO) {
        // Validate property exists
        Property propertyFromDb = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new APIException("Property not found with id: " + propertyId));

        // Set the ID to ensure update, not insert
        propertyDTO.setId(propertyId);
        
        // Use our mapper to convert DTO to entity, preserving existing data
        Property property = mapToProperty(propertyDTO);

        // Update category if provided
        if (propertyDTO.getCategoryId() != null) {
            // Validate category exists
            CategoryDTO category = CategoryUtil.getCategoryById(propertyDTO.getCategoryId());
            
            if (category == null) {
                throw new APIException("Category not found with id: " + propertyDTO.getCategoryId());
            }
            
            property.setCategoryId(propertyDTO.getCategoryId());
        }

        // Save updated property
        Property updatedProperty = propertyRepository.save(property);
        
        // Return converted DTO
        return mapToPropertyDTO(updatedProperty);
    }

    @Override
    public PropertyDTO deleteProperty(Long propertyId) {
        // Validate property exists
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new APIException("Property not found with id: " + propertyId));

        // Delete property
        propertyRepository.delete(property);

        // Return empty DTO with just the ID
        PropertyDTO emptyDto = new PropertyDTO();
        emptyDto.setId(propertyId);
        return emptyDto;
    }

    @Override
    public PropertyDTO updatePropertyImages(Long propertyId, MultipartFile[] images) throws IOException {
        // Validate property exists
        Property propertyFromDb = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new APIException("Property not found with id: " + propertyId));

        // Call file-service to upload images
        try {
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("propertyId", propertyId.toString());
            
            // Add files to the request
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    // Convert MultipartFile to Resource
                    ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };
                    body.add("files", resource);
                }
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Call file-service API
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "http://file-service/api/files/property/images",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Extract image URLs from response
                List<String> imageUrls = response.getBody().stream()
                    .filter(item -> item.containsKey("url") && item.get("url") != null)
                    .map(item -> item.get("url").toString())
                    .collect(Collectors.toList());
                
                // Update property with new image URLs
                propertyFromDb.setImages(imageUrls);
                Property updatedProperty = propertyRepository.save(propertyFromDb);
                
                logger.info("Updated property {} with {} new images", propertyId, imageUrls.size());
                
                // Return updated property DTO
                return mapToPropertyDTO(updatedProperty);
            } else {
                throw new APIException("Failed to upload images. Response: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error calling file-service: {}", e.getMessage());
            throw new APIException("Error updating property images: " + e.getMessage());
        }
    }

    @Override
    public PropertyResponse getPropertiesByLocation(String city, String state, String zipcode, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Validate at least one location parameter is provided
        if ((city == null || city.isEmpty()) && (state == null || state.isEmpty()) && (zipcode == null || zipcode.isEmpty())) {
            throw new APIException("At least one location parameter (city, state, or zipcode) must be provided");
        }

        // Create Pageable with sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Query properties by location
        Page<Property> pageProperties = propertyRepository.findByLocation(city, state, zipcode, pageDetails);
        List<Property> properties = pageProperties.getContent();

        // Map results to DTOs with our mapper function
        List<PropertyDTO> propertyDTOs = properties.stream()
                .map(this::mapToPropertyDTO)
                .collect(Collectors.toList());

        // Create response
        PropertyResponse propertyResponse = new PropertyResponse();
        propertyResponse.setContent(propertyDTOs);
        propertyResponse.setPageNumber(pageProperties.getNumber());
        propertyResponse.setPageSize(pageProperties.getSize());
        propertyResponse.setTotalElements(pageProperties.getTotalElements());
        propertyResponse.setTotalPages(pageProperties.getTotalPages());
        propertyResponse.setLastPage(pageProperties.isLast());

        return propertyResponse;
    }

    @Override
    public List<PropertyDTO> getPropertiesBySellerId(Long userId) {
        logger.info("Fetching properties for seller with user ID: {}", userId);
        List<Property> properties = propertyRepository.findByUserId(userId);
        
        return properties.stream()
                .map(this::mapToPropertyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PropertyDTO getPropertyById(Long id) {
        logger.info("Fetching property with ID: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new APIException("Property not found with id: " + id));

        return mapToPropertyDTO(property);
    }

    @Override
    @Transactional
    public PropertyDTO incrementPropertyViews(Long id) {
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        property.setTotalWatched(property.getTotalWatched() + 1);
        Property updatedProperty = propertyRepository.save(property);
        return mapToPropertyDTO(updatedProperty);
    }

    // Helper method to construct image URLs
    private String constructImageUrl(String imageName) {
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
    }

    // Create a method to map Property entity to PropertyDTO
    private PropertyDTO mapToPropertyDTO(Property property) {
        PropertyDTO dto = new PropertyDTO();
        dto.setId(property.getId());
        dto.setType(property.getType());
        dto.setTitle(property.getTitle());
        dto.setAddress(property.getAddress());
        dto.setPrice(property.getPrice());
        dto.setBedrooms(property.getBedrooms());
        dto.setBathrooms(property.getBathrooms());
        dto.setSqft(property.getSqft());
        dto.setLotSize(property.getLotSize());
        dto.setYearBuilt(property.getYearBuilt());
        dto.setDescription(property.getDescription());
        dto.setFeatures(property.getFeatures());
        dto.setImages(property.getImages());
        dto.setStatus(property.getStatus());
        dto.setDaysOnMarket(property.getDaysOnMarket());
        
        // Map Agent info
        AgentDTO agentDTO = new AgentDTO();
        agentDTO.setName(property.getAgentName());
        agentDTO.setPhone(property.getAgentPhone());
        agentDTO.setEmail(property.getAgentEmail());
        agentDTO.setImage(property.getAgentImage());
        agentDTO.setCompany(property.getAgentCompany());
        dto.setAgent(agentDTO);
        
        // Map Location
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setLat(property.getLatitude());
        locationDTO.setLng(property.getLongitude());
        dto.setLocation(locationDTO);
        
        // Map Neighborhood
        NeighborhoodDTO neighborhoodDTO = new NeighborhoodDTO();
        neighborhoodDTO.setName(property.getNeighborhoodName());
        neighborhoodDTO.setDescription(property.getNeighborhoodDescription());
        neighborhoodDTO.setAmenities(property.getNeighborhoodAmenities());
        neighborhoodDTO.setWalkScore(property.getWalkScore());
        neighborhoodDTO.setTransitScore(property.getTransitScore());
        neighborhoodDTO.setBikeScore(property.getBikeScore());
        dto.setNeighborhood(neighborhoodDTO);
        
        // Map AddressDetails
        AddressDetailsDTO addressDetailsDTO = new AddressDetailsDTO();
        addressDetailsDTO.setStreet(property.getStreet());
        addressDetailsDTO.setCity(property.getCity());
        addressDetailsDTO.setState(property.getState());
        addressDetailsDTO.setZip(property.getZip());
        addressDetailsDTO.setNeighborhood(property.getNeighborhoodName());
        dto.setAddressDetails(addressDetailsDTO);
        
        // Map backend-specific properties
        dto.setCategoryId(property.getCategoryId());
        dto.setForSale(property.isForSale());
        dto.setForRent(property.isForRent());
        dto.setRentPrice(property.getRentPrice());
        dto.setUserId(property.getUserId());
        
        // Set categoryName using CategoryUtil
        if (property.getCategoryId() != null) {
            CategoryDTO categoryDTO = CategoryUtil.getCategoryById(property.getCategoryId());
            if (categoryDTO != null) {
                dto.setCategoryName(categoryDTO.getName());
            }
        }
        
        return dto;
    }

    // Create a method to map PropertyDTO to Property entity
    private Property mapToProperty(PropertyDTO dto) {
        Property property = new Property();
        property.setId(dto.getId());
        property.setType(dto.getType());
        property.setTitle(dto.getTitle());
        property.setAddress(dto.getAddress());
        property.setPrice(dto.getPrice());
        property.setBedrooms(dto.getBedrooms());
        property.setBathrooms(dto.getBathrooms());
        property.setSqft(dto.getSqft());
        property.setLotSize(dto.getLotSize());
        property.setYearBuilt(dto.getYearBuilt());
        property.setDescription(dto.getDescription());
        property.setFeatures(dto.getFeatures());
        property.setImages(dto.getImages());
        property.setStatus(dto.getStatus());
        property.setDaysOnMarket(dto.getDaysOnMarket());
        
        // Map Agent info if provided
        if (dto.getAgent() != null) {
            property.setAgentName(dto.getAgent().getName());
            property.setAgentPhone(dto.getAgent().getPhone());
            property.setAgentEmail(dto.getAgent().getEmail());
            property.setAgentImage(dto.getAgent().getImage());
            property.setAgentCompany(dto.getAgent().getCompany());
        }
        
        // Map Location if provided
        if (dto.getLocation() != null) {
            property.setLatitude(dto.getLocation().getLat());
            property.setLongitude(dto.getLocation().getLng());
        }
        
        // Map Neighborhood if provided
        if (dto.getNeighborhood() != null) {
            property.setNeighborhoodName(dto.getNeighborhood().getName());
            property.setNeighborhoodDescription(dto.getNeighborhood().getDescription());
            property.setNeighborhoodAmenities(dto.getNeighborhood().getAmenities());
            property.setWalkScore(dto.getNeighborhood().getWalkScore());
            property.setTransitScore(dto.getNeighborhood().getTransitScore());
            property.setBikeScore(dto.getNeighborhood().getBikeScore());
        }
        
        // Map AddressDetails if provided
        if (dto.getAddressDetails() != null) {
            property.setStreet(dto.getAddressDetails().getStreet());
            property.setCity(dto.getAddressDetails().getCity());
            property.setState(dto.getAddressDetails().getState());
            property.setZip(dto.getAddressDetails().getZip());
            // Note: neighborhood name is already set from neighborhood object
        }
        
        // Map backend-specific properties
        property.setCategoryId(dto.getCategoryId());
        property.setForSale(dto.isForSale());
        property.setForRent(dto.isForRent());
        property.setRentPrice(dto.getRentPrice());
        property.setUserId(dto.getUserId());
        
        return property;
    }
}