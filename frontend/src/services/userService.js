import axios from 'axios';
import authHeader from './authHeader';

// API base URL - should be configured based on environment
const API_URL = 'http://localhost:8080/api/';

// User service functions
const userService = {
  // Get user information by ID (for public display - safe information only)
  getUserById: async (userId) => {
    try {
      const response = await axios.get(`${API_URL}users/public/${userId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi lấy thông tin người dùng';
    }
  },
  
  // Get current user's profile (for the logged-in user)
  getCurrentUserProfile: async () => {
    try {
      const response = await axios.get(`${API_URL}users/me`, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi lấy thông tin người dùng hiện tại';
    }
  },
  
  // Update current user's profile
  updateProfile: async (profileData) => {
    try {
      const response = await axios.put(`${API_URL}users/profile`, profileData, {
        headers: authHeader()
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi cập nhật thông tin cá nhân';
    }
  },
  
  // Change password
  changePassword: async (currentPassword, newPassword) => {
    try {
      const response = await axios.put(`${API_URL}users/password`, 
        { currentPassword, newPassword },
        { headers: authHeader() }
      );
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Lỗi khi thay đổi mật khẩu';
    }
  }
};

export default userService; 