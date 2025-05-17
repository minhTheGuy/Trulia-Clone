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

const paymentService = {
  // Xử lý thanh toán tiền thuê nhà
  processRentalPayment: async (paymentData) => {
    try {
      // Đảm bảo dữ liệu phù hợp với yêu cầu của API
      const formattedPaymentData = {
        ...paymentData,
        // Thêm các trường bắt buộc theo yêu cầu của PaymentController
        transactionId: paymentData.transaction_id || Date.now(), // Tạo ID tạm thời nếu không có
        paymentMethod: "STRIPE", // Giá trị enum mặc định là STRIPE
        // Chuyển đổi số tiền sang BigDecimal format cho Java
        amount: parseFloat(paymentData.amount),
        // Stripe configuration
        stripePublicKey: 'pk_test_51ROgtQR5Ad8YeKfV6ls2aEK1yDnUksGvlEn1Zbj9usi0faG2Rte3SgpNAJgZVwWt7BZCqUVOGxakVtwJHOQ9qYgK00GzDVFDiP',
        // Giữ các thông tin khác
        paymentMethodId: paymentData.payment_method_id,
        propertyId: paymentData.property_id,
        rentalPeriod: paymentData.rental_period,
        rentalStartDate: paymentData.rental_start_date,
        rentalEndDate: paymentData.rental_end_date,
        userId: paymentData.user_id,
        sellerId: paymentData.seller_id,
        description: `Thanh toán tiền thuê nhà - Thời hạn: ${paymentData.rental_period} tháng`
      };

      console.log('Sending payment data:', formattedPaymentData);

      // Updated endpoint from 'process-rental' to 'rental-payment'
      const response = await api.post('rental-payment', formattedPaymentData, {
        headers: authHeader()
      });
      
      return response.data;
    } catch (error) {
      console.error('Payment processing error:', error);
      
      // Handle specific error messages from backend
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.message) {
        throw new Error(error.message);
      } else {
        throw new Error('Failed to process rental payment');
      }
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