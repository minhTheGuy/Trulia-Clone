import axios from 'axios';
import authHeader from './authHeader';

// API base URL - should be configured based on environment
const API_URL = 'http://localhost:8080/api/properties/';

// Timeout duration in milliseconds
const TIMEOUT_DURATION = 10000;

// Create axios instance with default headers and timeout
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  timeout: TIMEOUT_DURATION, // 10 second timeout
  withCredentials: true // Enable sending cookies with CORS requests
});

// Create a consistent axios instance for direct calls
const consistentAxios = axios.create({
  timeout: TIMEOUT_DURATION,
});

// Add response interceptor for consistent error handling
const addErrorHandling = (axiosInstance) => {
  axiosInstance.interceptors.response.use(
    (response) => {
      return response;
    },
    (error) => {
      // Handle network errors or server unavailable errors
      if (error.code === 'ECONNABORTED' || !error.response) {
        return Promise.reject({
          response: {
            data: {
              message: 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại sau.'
            }
          }
        });
      }
      
      // Handle service unavailable errors
      if (error.response && error.response.status >= 500) {
        return Promise.reject({
          response: {
            data: {
              message: 'Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau.'
            }
          }
        });
      }
      
      return Promise.reject(error);
    }
  );
};

// Apply error handling to both axios instances
addErrorHandling(api);
addErrorHandling(consistentAxios);

// Add request interceptor to include auth token in every request if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Helper function for uploading property images
const uploadPropertyImages = async (propertyId, images, maxRetries = 2) => {
  let retries = 0;
  
  const attemptUpload = async () => {
    try {
      const formData = new FormData();
      images.forEach(image => {
        formData.append('images', image);
      });
      
      console.log(`Uploading ${images.length} images for property ID: ${propertyId}`);
      
      const response = await consistentAxios.put(`${API_URL}${propertyId}/images`, formData, {
        headers: {
          ...authHeader(),
          'Content-Type': 'multipart/form-data'
        }
      });
      
      console.log("Image upload successful:", response.data);
      return response.data;
    } catch (error) {
      console.error('Error uploading images:', error);
      if (retries < maxRetries) {
        retries++;
        console.log(`Retrying image upload (${retries}/${maxRetries})...`);
        return attemptUpload();
      }
      throw error.response?.data?.message || 'Failed to upload property images';
    }
  };
  
  return attemptUpload();
};

// Property service functions
const propertyService = {
  // Get all properties (public endpoint)
  getAllProperties: async (pageNumber = 0, pageSize = 10, sortBy = 'price', sortOrder = 'asc', keyword = '', category = '') => {
    try {
      const response = await api.get(`public?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}${keyword ? `&keyword=${keyword}` : ''}${category ? `&category=${category}` : ''}`);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch properties';
    }
  },

  // Get properties by category (public endpoint)
  getPropertiesByCategory: async (categoryId, pageNumber = 0, pageSize = 10, sortBy = 'price', sortOrder = 'asc') => {
    try {
      const response = await api.get(`public/categories/${categoryId}?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}`);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch properties by category';
    }
  },

  // Search properties by keyword (public endpoint)
  searchPropertiesByKeyword: async (keyword, pageNumber = 0, pageSize = 10, sortBy = 'price', sortOrder = 'asc', signal) => {
    try {
      const config = {
        signal // Pass abort signal to request
      };
      
      // Decode the keyword before using it in the URL since it's already encoded
      const decodedKeyword = decodeURIComponent(keyword);
      
      // Create URL parameters object
      const params = new URLSearchParams({
        pageNumber: pageNumber.toString(),
        pageSize: pageSize.toString(),
        sortBy: sortBy,
        sortOrder: sortOrder,
        keyword: decodedKeyword
      });
      
      const response = await api.get(
        `public/keyword/${keyword}?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}`,
        config
      );
      return response.data;
    } catch (error) {
      // Check if this is an abort error first
      if (error.name === 'AbortError' || error.code === 'ECONNABORTED' || 
          (error.message && error.message.includes('aborted'))) {
        console.log('Request was aborted intentionally');
        const abortError = new Error('Request aborted');
        abortError.name = 'AbortError';
        return { aborted: true, content: [], pageNumber: 0, totalElements: 0, totalPages: 0, lastPage: true };
      }
      
      // Then check for other errors
      if (error.response?.data?.message) {
        throw error.response.data.message;
      } else if (error.message) {
        throw error.message;
      } else {
        throw 'Failed to search properties';
      }
    }
  },

  // Search properties by filters (public endpoint)
  searchPropertiesByFilters: async (filters) => {
    try {
      const { 
        pageNumber = 0, 
        pageSize = 10, 
        sortBy = 'price', 
        sortOrder = 'asc',
        minPrice, 
        maxPrice, 
        minBedrooms, 
        minBathrooms, 
        minSquareFootage,
        propertyType,
        forSale,
        forRent,
        signal // Extract the signal from filters
      } = filters;

      let url = `public/search?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}`;
      
      if (minPrice !== undefined) url += `&minPrice=${minPrice}`;
      if (maxPrice !== undefined) url += `&maxPrice=${maxPrice}`;
      if (minBedrooms !== undefined) url += `&minBedrooms=${minBedrooms}`;
      if (minBathrooms !== undefined) url += `&minBathrooms=${minBathrooms}`;
      if (minSquareFootage !== undefined) url += `&minSquareFootage=${minSquareFootage}`;
      if (propertyType !== undefined) url += `&propertyType=${propertyType}`;
      if (forSale !== undefined) url += `&forSale=${forSale}`;
      if (forRent !== undefined) url += `&forRent=${forRent}`;

      const config = {
        signal // Pass abort signal to request
      };

      const response = await api.get(url, config);
      return response.data;
    } catch (error) {
      // Handle abort errors specifically
      if (error.name === 'AbortError' || error.code === 'ECONNABORTED') {
        throw { name: 'AbortError', message: 'Request was cancelled' };
      }
      throw error.response?.data?.message || 'Failed to search properties';
    }
  },

  // Get properties by location (public endpoint)
  getPropertiesByLocation: async (location, pageNumber = 0, pageSize = 10, sortBy = 'price', sortOrder = 'asc') => {
    try {
      const { city, state, zipcode } = location;
      let url = `public/location?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}`;
      
      if (city) url += `&city=${city}`;
      if (state) url += `&state=${state}`;
      if (zipcode) url += `&zipcode=${zipcode}`;

      const response = await api.get(url);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch properties by location';
    }
  },

  // Add a new property (protected endpoint)
  addProperty: async (categoryId, propertyData) => {
    try {
      const response = await api.post(`categories/${categoryId}/property`, propertyData);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to add property';
    }
  },

  // Update property images (protected endpoint)
  updatePropertyImages: async (propertyId, images) => {
    try {
      const formData = new FormData();
      for (let i = 0; i < images.length; i++) {
        formData.append('images', images[i]);
      }

      const response = await consistentAxios.put(`${API_URL}${propertyId}/images`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to update property images';
    }
  },

  // Tìm kiếm bất động sản với các bộ lọc
  searchProperties: async (filters) => {
    try {
      // Extract signal from filters to avoid sending it as a query parameter
      const { signal, ...queryParams } = filters;
      
      const config = {
        params: queryParams,
        headers: {
          'Accept': 'application/json'
        },
        signal // Pass abort signal
      };
      
      const response = await consistentAxios.get(API_URL + 'public/search', config);
      return response.data;
    } catch (error) {
      // Check if this is an abort error first
      if (error.name === 'AbortError' || error.code === 'ECONNABORTED' || 
          (error.message && error.message.includes('aborted'))) {
        console.log('Request was aborted intentionally');
        return { aborted: true, content: [], pageNumber: 0, totalElements: 0, totalPages: 0, lastPage: true };
      }
      
      // Then check for other errors
      if (error.response?.data?.message) {
        throw error.response.data.message;
      } else if (error.message) {
        throw error.message;
      } else {
        throw 'Failed to search properties';
      }
    }
  },

  // Lấy thông tin bất động sản theo ID
  getPropertyById: async (id) => {
    try {
      // Đảm bảo không có dấu "/" trùng lặp trong URL
      const response = await api.get(`${API_URL.endsWith('/') ? API_URL.slice(0, -1) : API_URL}/public/${id}`, {
        headers: {
          ...authHeader(),
          'Accept': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to get property by ID';
    }
  },

  // Lấy danh sách bất động sản của một người bán cụ thể theo ID
  getPropertiesBySellerId: async (sellerId) => {
    try {
      // Đảm bảo không có dấu "/" trùng lặp trong URL
      const response = await api.get(`${API_URL.endsWith('/') ? API_URL.slice(0, -1) : API_URL}/seller/${sellerId}`, {
        headers: {
          ...authHeader(),
          'Accept': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to get properties by seller ID';
    }
  },

  // Tạo tin đăng bất động sản mới
  createProperty: async (propertyData, images) => {
    try {
      // Make sure categoryId is set (using a default if not provided)
      if (!propertyData.categoryId) {
        propertyData.categoryId = 1; // Default to category ID 1
      }
      
      // Ensure all required fields are properly formatted
      const formattedPropertyData = {
        ...propertyData,
        // Make sure numeric fields are properly typed
        price: typeof propertyData.price === 'string' ? parseFloat(propertyData.price) : propertyData.price,
        bedrooms: typeof propertyData.bedrooms === 'string' ? parseInt(propertyData.bedrooms) : propertyData.bedrooms,
        bathrooms: typeof propertyData.bathrooms === 'string' ? parseFloat(propertyData.bathrooms) : propertyData.bathrooms,
        sqft: typeof propertyData.sqft === 'string' ? parseFloat(propertyData.sqft) : propertyData.sqft,
        yearBuilt: typeof propertyData.yearBuilt === 'string' ? parseInt(propertyData.yearBuilt) : propertyData.yearBuilt,
        
        // Ensure boolean fields are properly typed
        forSale: !!propertyData.forSale,
        forRent: !!propertyData.forRent,
        
        // Set default status if not provided
        status: propertyData.status || 'PENDING',
        
        // Ensure arrays are properly initialized
        features: propertyData.features || [],
        images: propertyData.images || []
      };
      
      console.log("Sending property data:", formattedPropertyData);
      
      // Get authorization token
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Authentication required. Please login again.');
      }
      
      // Bước 1: Tạo bất động sản - try both endpoints
      let response;
      try {
        // Try with the direct endpoint first
        response = await api.post(API_URL, formattedPropertyData, {
          headers: {
            ...authHeader(),
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        });
      } catch (firstError) {
        console.log("First attempt failed, trying alternate endpoint", firstError);
        // If the first attempt fails, try with the explicit endpoint
        response = await api.post(`${API_URL}create`, formattedPropertyData, {
          headers: {
            ...authHeader(),
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        });
      }
      
      const propertyId = response.data.id;
      console.log("Property created with ID:", propertyId);
      
      // Bước 2: Tải lên hình ảnh nếu có
      if (images && images.length > 0 && propertyId) {
        console.log("Uploading images for property:", propertyId);
        try {
          await uploadPropertyImages(propertyId, images);
        } catch (imageError) {
          console.error("Image upload failed but property was created:", imageError);
          // Continue even if image upload fails
        }
      }
      
      return response.data;
    } catch (error) {
      console.error('Property creation error:', error);
      if (error.response) {
        console.error('Response data:', error.response.data);
        console.error('Response status:', error.response.status);
        console.error('Response headers:', error.response.headers);
      }
      throw error.response?.data?.message || 'Failed to create property';
    }
  },

  // Update property
  updateProperty: async (id, propertyData, images) => {
    try {
      // Format property data consistently with createProperty
      const formattedPropertyData = {
        ...propertyData,
        // Make sure numeric fields are properly typed
        price: typeof propertyData.price === 'string' ? parseFloat(propertyData.price) : propertyData.price,
        bedrooms: typeof propertyData.bedrooms === 'string' ? parseInt(propertyData.bedrooms) : propertyData.bedrooms,
        bathrooms: typeof propertyData.bathrooms === 'string' ? parseFloat(propertyData.bathrooms) : propertyData.bathrooms,
        sqft: typeof propertyData.sqft === 'string' ? parseFloat(propertyData.sqft) : propertyData.sqft,
        yearBuilt: typeof propertyData.yearBuilt === 'string' ? parseInt(propertyData.yearBuilt) : propertyData.yearBuilt,
        
        // Ensure boolean fields are properly typed
        forSale: !!propertyData.forSale,
        forRent: !!propertyData.forRent,
        
        // Set default status if not provided
        status: propertyData.status || 'PENDING',
        
        // Ensure arrays are properly initialized
        features: propertyData.features || [],
        images: propertyData.images || []
      };
      
      console.log(`Updating property ID ${id} with data:`, formattedPropertyData);
      
      // Bước 1: Cập nhật thông tin bất động sản
      // Đảm bảo không có dấu "/" trùng lặp trong URL
      const response = await api.put(`${API_URL.endsWith('/') ? API_URL.slice(0, -1) : API_URL}/${id}`, formattedPropertyData, {
        headers: {
          ...authHeader(),
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      });
      
      // Bước 2: Tải lên hình ảnh mới nếu có
      if (images && images.length > 0) {
        await uploadPropertyImages(id, images);
      }
      
      return response.data;
    } catch (error) {
      console.error('Property update error:', error);
      if (error.response) {
        console.error('Response data:', error.response.data);
        console.error('Response status:', error.response.status);
      }
      throw error.response?.data?.message || 'Failed to update property';
    }
  },

  // Xóa tin đăng bất động sản
  deleteProperty: async (id) => {
    try {
      // Đảm bảo không có dấu "/" trùng lặp trong URL
      await api.delete(`${API_URL.endsWith('/') ? API_URL.slice(0, -1) : API_URL}/${id}`, {
        headers: authHeader()
      });
      return id;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to delete property';
    }
  },

  // Schedule a tour for a property
  scheduleTour: async (tourData) => {
    try {
      const response = await api.post('schedule-tour', tourData, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to schedule tour';
    }
  },

  // Request information about a property
  requestInfo: async (requestData) => {
    try {
      const response = await api.post('request-info', requestData, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to request information';
    }
  },
  
  // Get all tours for properties owned by a seller
  getSellerTours: async (userId) => {
    try {
      const response = await api.get(`users/${userId}/tours`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch seller tours';
    }
  },
  
  // Get tours for a seller with specific status
  getSellerToursByStatus: async (userId, status) => {
    try {
      const response = await api.get(`users/${userId}/tours/status/${status}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch seller tours by status';
    }
  },
  
  // Get all info requests for properties owned by a seller
  getSellerInfoRequests: async (userId) => {
    try {
      const response = await api.get(`sellers/${userId}/info-requests`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch seller info requests';
    }
  },
  
  // Get info requests for a seller with specific status 
  getSellerInfoRequestsByStatus: async (userId, status) => {
    try {
      const response = await api.get(`sellers/${userId}/info-requests/status/${status}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch seller info requests by status';
    }
  },
  
  // Update tour status
  updateTourStatus: async (tourId, status) => {
    try {
      const response = await api.put(`tours/${tourId}/status/${status}`, null, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to update tour status';
    }
  },
  
  // Cancel a tour
  cancelTour: async (tourId) => {
    try {
      await api.delete(`tours/${tourId}`, {
        headers: authHeader()
      });
      return true;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to cancel tour';
    }
  },
  
  // Các phương thức liên quan đến thuê nhà và thanh toán đã được chuyển
  // sang rentalService.js và paymentService.js

  // Get saved properties for the current user
  getSavedProperties: () => {
    return axios.get(API_URL + 'saved', { headers: authHeader() });
  },

  // Save a property
  saveProperty: (propertyId) => {
    return axios.post(API_URL + 'save/' + propertyId, {}, { headers: authHeader() });
  },

  // Unsave/remove a property from saved list
  unsaveProperty: (propertyId) => {
    return axios.delete(API_URL + 'unsave/' + propertyId, { headers: authHeader() });
  },

  // Check if a property is saved by the user
  isPropertySaved: (propertyId) => {
    return axios.get(API_URL + 'is-saved/' + propertyId, { headers: authHeader() });
  }
};

export default propertyService; 