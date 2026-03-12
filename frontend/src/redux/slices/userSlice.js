import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import userService from '../../services/userService';

const initialState = {
  userDetails: null,
  loading: false,
  error: null,
};

// Async thunks for user actions
export const fetchUserById = createAsyncThunk(
  'users/fetchById',
  async (userId, { rejectWithValue }) => {
    try {
      const response = await userService.getUserById(userId);
      return response;
    } catch (error) {
      return rejectWithValue(error.message || 'Lỗi khi lấy thông tin người dùng');
    }
  }
);

export const fetchCurrentUserProfile = createAsyncThunk(
  'users/fetchCurrentProfile',
  async (_, { rejectWithValue }) => {
    try {
      const response = await userService.getCurrentUserProfile();
      return response;
    } catch (error) {
      return rejectWithValue(error.message || 'Lỗi khi lấy thông tin cá nhân');
    }
  }
);

export const updateUserProfile = createAsyncThunk(
  'users/updateProfile',
  async (profileData, { rejectWithValue }) => {
    try {
      const response = await userService.updateProfile(profileData);
      return response;
    } catch (error) {
      return rejectWithValue(error.message || 'Lỗi khi cập nhật thông tin cá nhân');
    }
  }
);

const userSlice = createSlice({
  name: 'users',
  initialState,
  reducers: {
    clearUserDetails: (state) => {
      state.userDetails = null;
    },
    clearUserError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch user by ID
      .addCase(fetchUserById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserById.fulfilled, (state, action) => {
        state.loading = false;
        state.userDetails = action.payload;
      })
      .addCase(fetchUserById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Fetch current user profile
      .addCase(fetchCurrentUserProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCurrentUserProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.userDetails = action.payload;
      })
      .addCase(fetchCurrentUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Update user profile
      .addCase(updateUserProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateUserProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.userDetails = action.payload;
      })
      .addCase(updateUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearUserDetails, clearUserError } = userSlice.actions;

export default userSlice.reducer; 