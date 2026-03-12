import axios from "axios";
import authHeader from './authHeader';

// API base URL - should be configured based on environment
const API_URL = "http://localhost:8080/api/auth/";

// Create axios instance with default headers
const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
    "Accept": "application/json"
  },
  withCredentials: true, // Enable sending cookies with CORS requests
});

// Add request interceptor to include auth token in every request if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Authentication service functions
const authService = {
  // Register user
  register: async (
    username,
    email,
    password,
    firstName,
    lastName,
    phoneNumber
  ) => {
    try {
      const response = await api.post("signup", {
        username,
        email,
        password,
        firstName,
        lastName,
        phoneNumber,
        roles: ["ROLE_USER"], // Default role with correct format for backend enum
      });
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || "Registration failed";
    }
  },

  // Login user
  login: async (username, password) => {
    try {
      const response = await api.post("signin", { username, password });

      // Store token and user data from the response
      if (response.data && response.data.token) {
        localStorage.setItem("token", response.data.token);
        localStorage.setItem("user", JSON.stringify(response.data));
      }

      return response.data;
    } catch (error) {
      console.error("Login error:", error);
      throw error.response?.data?.message || "Invalid username or password";
    }
  },

  // Logout user
  logout: async () => {
    try {
      await api.post("signout");
      localStorage.removeItem("user");
      localStorage.removeItem("token");
    } catch (error) {
      console.error("Logout error:", error);
      // Still clear local storage even if API call fails
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    }
  },

  // Refresh token
  refreshToken: async () => {
    try {
      const response = await api.post("refresh");

      if (response.data.token) {
        localStorage.setItem("token", response.data.token);
        localStorage.setItem("user", JSON.stringify(response.data));
      }

      return response.data;
    } catch (error) {
      authService.logout();
      throw error;
    }
  },

  // Get current user
  getCurrentUser: () => {
    const userStr = localStorage.getItem("user");
    if (userStr) {
      return JSON.parse(userStr);
    }
    return null;
  },

  // Check if user is authenticated
  isLoggedIn: () => {
    return !!localStorage.getItem("token");
  },

  // Verify email with verification token
  verifyEmail: async (token) => {
    try {
      const response = await api.get(`verify?token=${token}`);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || "Email verification failed";
    }
  },

  changePassword: (currentPassword, newPassword) => {
    return axios.post(API_URL + 'password/change', 
      { currentPassword, newPassword },
      { headers: authHeader() }
    );
  },

  // Store OAuth2 token and reconstruct the user object from JWT claims
  loginWithToken: (token) => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const user = {
        token,
        type: 'Bearer',
        id: payload.userId,
        username: payload.sub,
        email: payload.email,
        roles: Array.isArray(payload.roles) ? payload.roles : [],
      };
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      return user;
    } catch (e) {
      console.error('Failed to parse OAuth2 token:', e);
      throw new Error('Invalid token received from OAuth2 provider');
    }
  },
};

export default authService;
