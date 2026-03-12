import axios from 'axios';
import authHeader from './authHeader';

// API base URL
const API_URL = 'http://localhost:8080/api/properties/rentals';

// Axios instance configuration
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  timeout: 10000, // 10 second timeout
  withCredentials: true
});

// Add request interceptor for auth token
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

const rentalService = {
  // Rent a property
  rentProperty: async (rentalData) => {
    try {
      const response = await api.post('', rentalData, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      console.error('Rental creation error:', error);
      throw error.response?.data?.message || 'Failed to rent property';
    }
  },
  
  // Get rental history for a user
  getUserRentals: async (userId) => {
    try {
      const response = await api.get(`users/${userId}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch user rentals';
    }
  },
  
  // Get rentals for a specific property
  getPropertyRentals: async (propertyId) => {
    try {
      const response = await api.get(`properties/${propertyId}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch property rentals';
    }
  },
  
  // Cancel a rental
  cancelRental: async (rentalId) => {
    try {
      const response = await api.put(`${rentalId}/cancel`, null, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to cancel rental';
    }
  },

  // Extend a rental
  extendRental: async (rentalId, extensionData) => {
    try {
      const response = await api.put(`${rentalId}/extend`, extensionData, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to extend rental';
    }
  },

  // Get rental details
  getRentalDetails: async (rentalId) => {
    try {
      const response = await api.get(`${rentalId}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch rental details';
    }
  }
};

export default rentalService; 