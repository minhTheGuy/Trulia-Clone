import axios from 'axios';
import authHeader from './authHeader';

// API base URL
const API_URL = 'http://localhost:8080/api/payments/';

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

const stripeApi = axios.create({
  baseURL: 'http://localhost:8080/api/stripe/',
  headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
  timeout: 10000,
  withCredentials: true,
});

stripeApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

const paymentService = {
  // Create a Stripe Checkout Session and return { sessionId, sessionUrl }
  createCheckoutSession: async ({ transactionId, propertyId, amount, paymentMethod = 'STRIPE', rentalPeriod, rentalStartDate, rentalEndDate }) => {
    try {
      const response = await stripeApi.post('create-rental-checkout-session', {
        transactionId,
        propertyId,
        amount: parseFloat(amount),
        paymentMethod,
        rentalPeriod: String(rentalPeriod),
        rentalStartDate,
        rentalEndDate,
      }, { headers: authHeader() });
      return response.data;
    } catch (error) {
      const msg = error.response?.data?.message || error.message || 'Failed to create checkout session';
      throw new Error(msg);
    }
  },

  // Lấy lịch sử thanh toán của người dùng
  getUserPayments: async (userId) => {
    try {
      const response = await api.get(`users/${userId}/payments`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch payment history';
    }
  },

  // Lấy chi tiết thanh toán
  getPaymentDetails: async (paymentId) => {
    try {
      const response = await api.get(`${paymentId}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch payment details';
    }
  }
};

export default paymentService; 