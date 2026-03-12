import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

// Base API URL - adjust this to match your backend API URL
const API_URL = 'http://localhost:8080/api/properties/';

// Async thunk to fetch saved homes from the API
export const fetchSavedHomes = createAsyncThunk(
  'savedHomes/fetch',
  async (userId, { rejectWithValue }) => {
    try {
      // Get the authentication token from local storage
      const token = localStorage.getItem('token');
      if (!token) {
        return rejectWithValue('Authentication required');
      }

      // Make the API request to get saved homes for the user
      const response = await axios.get(`${API_URL}users/${userId}/saved-homes`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      return response.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || error.message || 'Failed to fetch saved homes'
      );
    }
  }
);

// Async thunk to add a property to saved homes on the server
export const saveHomeToServer = createAsyncThunk(
  'savedHomes/save',
  async ({ userId, propertyId }, { rejectWithValue }) => {
    try {
      // Get the authentication token from local storage
      const token = localStorage.getItem('token');
      if (!token) {
        return rejectWithValue('Authentication required');
      }

      // Make the API request to save a home
      const response = await axios.post(`${API_URL}users/${userId}/saved-homes/${propertyId}`, {}, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      return response.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || error.message || 'Failed to save home'
      );
    }
  }
);

// Async thunk to remove a property from saved homes on the server
export const removeHomeFromServer = createAsyncThunk(
  'savedHomes/remove',
  async ({ userId, propertyId }, { rejectWithValue }) => {
    try {
      // Get the authentication token from local storage
      const token = localStorage.getItem('token');
      if (!token) {
        return rejectWithValue('Authentication required');
      }

      // Make the API request to remove a saved home
      await axios.delete(`${API_URL}users/${userId}/saved-homes/${propertyId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      return propertyId;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || error.message || 'Failed to remove saved home'
      );
    }
  }
);

const initialState = {
  savedHomes: [],
  loading: false,
  error: null
};

const savedHomesSlice = createSlice({
  name: 'savedHomes',
  initialState,
  reducers: {
    addSavedHome: (state, action) => {
      // Check if property is already saved
      const exists = state.savedHomes.some(home => home.id === action.payload.id);
      if (!exists) {
        state.savedHomes.push(action.payload);
      }
    },
    removeSavedHome: (state, action) => {
      state.savedHomes = state.savedHomes.filter(home => home.id !== action.payload);
    },
    setSavedHomes: (state, action) => {
      state.savedHomes = action.payload;
    },
    clearSavedHomes: (state) => {
      state.savedHomes = [];
    }
  },
  extraReducers: (builder) => {
    builder
      // Handle fetchSavedHomes
      .addCase(fetchSavedHomes.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchSavedHomes.fulfilled, (state, action) => {
        state.loading = false;
        state.savedHomes = action.payload;
      })
      .addCase(fetchSavedHomes.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Handle saveHomeToServer
      .addCase(saveHomeToServer.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(saveHomeToServer.fulfilled, (state, action) => {
        state.loading = false;
        // If server returns the full property object, add it to savedHomes
        if (action.payload && action.payload.id) {
          const exists = state.savedHomes.some(home => home.id === action.payload.id);
          if (!exists) {
            state.savedHomes.push(action.payload);
          }
        }
      })
      .addCase(saveHomeToServer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Handle removeHomeFromServer
      .addCase(removeHomeFromServer.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(removeHomeFromServer.fulfilled, (state, action) => {
        state.loading = false;
        state.savedHomes = state.savedHomes.filter(home => home.id !== action.payload);
      })
      .addCase(removeHomeFromServer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { addSavedHome, removeSavedHome, setSavedHomes, clearSavedHomes } = savedHomesSlice.actions;

export default savedHomesSlice.reducer; 