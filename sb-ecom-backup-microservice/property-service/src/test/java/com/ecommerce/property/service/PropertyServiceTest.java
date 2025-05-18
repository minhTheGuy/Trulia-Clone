package com.ecommerce.property.service;

import com.ecommerce.property.exceptions.APIException;
import com.ecommerce.property.exceptions.ResourceNotFoundException;
import com.ecommerce.property.model.Property;
import com.ecommerce.property.model.PropertyCategory;
import com.ecommerce.property.payload.dto.AddressDetailsDTO;
import com.ecommerce.property.payload.dto.PropertyDTO;
import com.ecommerce.property.payload.request.PropertyRequest;
import com.ecommerce.property.payload.response.PagedResponse;
import com.ecommerce.property.repository.PropertyRepository;
import com.ecommerce.property.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private Property property;
    private PropertyDTO propertyDTO;
    private PropertyRequest propertyRequest;

    @BeforeEach
    public void setup() {
        // Setup property
        property = new Property();
        property.setId(1L);
        property.setTitle("Beautiful House");
        property.setDescription("A beautiful house in the city center");
        property.setPrice(250000L);
        property.setBedrooms(3);
        property.setBathrooms(2);
        property.setSqft(1500.0);
        property.setYearBuilt(2015);
        property.setAddress("123 Main St");
        property.setCity("New York");
        property.setState("NY");
        property.setZip("10001");
        property.setForSale(true);
        property.setForRent(false);
        property.setStatus("ACTIVE");
        property.setCategoryId(PropertyCategory.BIET_THU.getId());
        property.setCreatedAt(LocalDateTime.now());
        property.setUpdatedAt(LocalDateTime.now());

        // Setup PropertyDTO
        propertyDTO = PropertyDTO.builder()
                .title("Beautiful House")
                .description("A beautiful house in the city center")
                .price(250000L)
                .bedrooms(3)
                .bathrooms(2)
                .sqft(1500.0)
                .yearBuilt(2015)
                .address("123 Main St")
                .addressDetails(new AddressDetailsDTO(
                        "123 Main St", "New York", "NY", "10001", "USA"))
                .forSale(true)
                .forRent(false)
                .categoryId(PropertyCategory.BIET_THU.getId())
                .build();

    }


    @Test
    public void testGetPropertyById_NotFound() {
        // Arrange
        when(propertyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(APIException.class, () -> propertyService.getPropertyById(1L));
        verify(propertyRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateProperty_Success() {
        // Arrange
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        PropertyDTO result = propertyService.addProperty(PropertyCategory.BIET_THU.getId(), propertyDTO);
        System.out.println("PropertyDTO: " + result);
        // Assert
        assertNotNull(result);
        assertEquals(propertyDTO.getTitle(), result.getTitle());
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    public void testUpdateProperty_Success() {
        // Arrange
        when(propertyRepository.findById(anyLong())).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenReturn(property);

        PropertyDTO updateRequest = new PropertyDTO();
        updateRequest.setTitle("Updated House");
        updateRequest.setDescription("An updated house in the city center");
        updateRequest.setPrice(250000L);
        updateRequest.setCategoryId(1L);

        // Act
        PropertyDTO result = propertyService.updateProperty(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(propertyDTO.getTitle(), result.getTitle());
        verify(propertyRepository, times(1)).findById(1L);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    public void testDeleteProperty_Success() {
        // Arrange
        when(propertyRepository.findById(anyLong())).thenReturn(Optional.of(property));
        doNothing().when(propertyRepository).delete(any(Property.class));

        // Act
        propertyService.deleteProperty(1L);

        // Assert
        verify(propertyRepository, times(1)).findById(1L);
        verify(propertyRepository, times(1)).delete(property);
    }

    @Test
    public void testDeleteProperty_NotFound() {
        // Arrange
        when(propertyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(APIException.class, () -> {
            propertyService.deleteProperty(1L);
        });

        verify(propertyRepository, never()).delete(any(Property.class));
    }
}

