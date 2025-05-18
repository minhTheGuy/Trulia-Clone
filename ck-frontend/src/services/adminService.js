import axios from 'axios';
import authHeader from './authHeader';

// API base URL - should be configured based on environment
const API_URL = 'http://localhost:8080/api/admin/';

// Admin service functions
const adminService = {
  // User Management
  getAllUsers: async (pageNumber = 0, pageSize = 10) => {
    try {
      const response = await axios.get(`${API_URL}users?pageNumber=${pageNumber}&pageSize=${pageSize}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi lấy danh sách người dùng';
    }
  },

  getUserById: async (userId) => {
    try {
      const response = await axios.get(`${API_URL}users/${userId}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi lấy thông tin người dùng';
    }
  },

  updateUserStatus: async (userId, isActive) => {
    try {
      const response = await axios.put(`${API_URL}users/${userId}/status`, 
        { active: isActive },
        { headers: authHeader() }
      );
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi cập nhật trạng thái người dùng';
    }
  },

  updateUserRoles: async (userId, roles) => {
    try {
      const response = await axios.put(`${API_URL}users/${userId}/roles`, 
        { roles },
        { headers: authHeader() }
      );
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi cập nhật vai trò người dùng';
    }
  },

  // New method for updating seller role
  updateSellerRole: async (userId, isSellerRole) => {
    try {
      const response = await axios.put(`${API_URL}users/${userId}/seller-role`, 
        { sellerRole: isSellerRole },
        { headers: authHeader() }
      );
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi cập nhật quyền môi giới';
    }
  },

  // New method for updating broker role
  updateBrokerRole: async (userId, isBrokerRole) => {
    try {
      const response = await axios.put(`${API_URL}users/${userId}/broker-role`, 
        { brokerRole: isBrokerRole },
        { headers: authHeader() }
      );
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi cập nhật quyền môi giới';
    }
  },

  // Revenue Management
  getRevenueStatistics: async (timeRange = 'month', year, month, quarter) => {
    try {
      let url = `${API_URL}revenue?timeRange=${timeRange}&year=${year}`;
      
      if (timeRange === 'month' && month) {
        url += `&month=${month}`;
      } else if (timeRange === 'quarter' && quarter) {
        url += `&quarter=${quarter}`;
      }
      
      const response = await axios.get(url, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi lấy thống kê doanh thu';
    }
  },

  getRevenueBySource: async (timeRange = 'month', year, month, quarter) => {
    try {
      let url = `${API_URL}revenue/by-source?timeRange=${timeRange}&year=${year}`;
      
      if (timeRange === 'month' && month) {
        url += `&month=${month}`;
      } else if (timeRange === 'quarter' && quarter) {
        url += `&quarter=${quarter}`;
      }
      
      const response = await axios.get(url, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi lấy doanh thu theo nguồn';
    }
  },

  getTransactions: async (filters) => {
    try {
      const response = await axios.get(`${API_URL}transactions`, {
        headers: authHeader(),
        params: {
          ...filters,
          startDate: null,
          endDate: null
        }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch transactions';
    }
  },

  getTransactionStats: async (timeRange) => {
    try {
      const response = await axios.get(`${API_URL}/transactions/stats`, {
        headers: authHeader(),
        params: { timeRange }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch transaction statistics';
    }
  },

  getRevenueStats: async (timeRange) => {
    try {
      const response = await axios.get(`${API_URL}/revenue/stats`, {
        headers: authHeader(),
        params: { timeRange }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch revenue statistics';
    }
  },

  updateTransactionStatus: async (transactionId, status) => {
    try {
      const response = await axios.put(`${API_URL}/transactions/${transactionId}/status`, 
        { status },
        { headers: authHeader() }
      );
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to update transaction status';
    }
  },

  getTransactionById: async (transactionId) => {
    try {
      const response = await axios.get(`${API_URL}/transactions/${transactionId}`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Failed to fetch transaction details';
    }
  }
};

export default adminService; 