import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

// Base API URL - adjust to match your backend API URL
const API_URL = 'http://localhost:8080/api/users/';

// Async thunk to fetch saved searches from the API
export const fetchSavedSearches = createAsyncThunk(
  'savedSearches/fetch',
  async (userId, { rejectWithValue }) => {
    try {
      // Get the authentication token from local storage
      const token = localStorage.getItem('token');
      if (!token) {
        return rejectWithValue('Authentication required');
      }

      // Make the API request to get saved searches for the user
      const response = await axios.get(`${API_URL}${userId}/saved-searches`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      return response.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || error.message || 'Failed to fetch saved searches'
      );
    }
  }
);

// Async thunk to save a search to the server
export const saveSearchToServer = createAsyncThunk(
  'savedSearches/save',
  async ({ userId, searchData }, { rejectWithValue }) => {
    try {
      // Get the authentication token from local storage
      const token = localStorage.getItem('token');
      if (!token) {
        return rejectWithValue('Authentication required');
      }

      // Make the API request to save a search
      const response = await axios.post(`${API_URL}${userId}/saved-searches`, searchData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      return response.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || error.message || 'Failed to save search'
      );
    }
  }
);

// Async thunk to delete a saved search from the server
export const deleteSavedSearch = createAsyncThunk(
  'savedSearches/delete',
  async ({ userId, searchId }, { rejectWithValue }) => {
    try {
      // Get the authentication token from local storage
      const token = localStorage.getItem('token');
      if (!token) {
        return rejectWithValue('Authentication required');
      }

      // Make the API request to delete a saved search
      await axios.delete(`${API_URL}${userId}/saved-searches/${searchId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      return searchId;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || error.message || 'Failed to delete saved search'
      );
    }
  }
);

const initialState = {
  savedSearches: [],
  loading: false,
  error: null,
  success: false
};

const savedSearchesSlice = createSlice({
  name: 'savedSearches',
  initialState,
  reducers: {
    addSavedSearch: (state, action) => {
      state.savedSearches.push(action.payload);
    },
    removeSavedSearch: (state, action) => {
      state.savedSearches = state.savedSearches.filter(search => search.id !== action.payload);
    },
    clearSavedSearches: (state) => {
      state.savedSearches = [];
    },
    clearError: (state) => {
      state.error = null;
    },
    clearSuccess: (state) => {
      state.success = false;
    }
  },
  extraReducers: (builder) => {
    builder
      // Handle fetchSavedSearches
      .addCase(fetchSavedSearches.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchSavedSearches.fulfilled, (state, action) => {
        state.loading = false;
        state.savedSearches = action.payload;
      })
      .addCase(fetchSavedSearches.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Handle saveSearchToServer
      .addCase(saveSearchToServer.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.success = false;
      })
      .addCase(saveSearchToServer.fulfilled, (state, action) => {
        state.loading = false;
        state.savedSearches.push(action.payload);
        state.success = true;
      })
      .addCase(saveSearchToServer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.success = false;
      })
      
      // Handle deleteSavedSearch
      .addCase(deleteSavedSearch.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteSavedSearch.fulfilled, (state, action) => {
        state.loading = false;
        state.savedSearches = state.savedSearches.filter(
          search => search.id !== action.payload
        );
        state.success = true;
      })
      .addCase(deleteSavedSearch.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { 
  addSavedSearch, 
  removeSavedSearch, 
  clearSavedSearches,
  clearError,
  clearSuccess
} = savedSearchesSlice.actions;

export default savedSearchesSlice.reducer; 