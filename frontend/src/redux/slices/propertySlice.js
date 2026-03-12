import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import propertyService from '../../services/propertyService';

const initialState = {
  properties: [],
  sellerProperties: [],
  selectedProperty: null,
  loading: false,
  error: null,
  pagination: {
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
    lastPage: true
  },
  success: false
};

// Async thunks for property actions
export const fetchProperties = createAsyncThunk(
  'properties/fetchAll',
  async ({ pageNumber, pageSize, sortBy, sortOrder, keyword, category }, { rejectWithValue }) => {
    try {
      return await propertyService.getAllProperties(pageNumber, pageSize, sortBy, sortOrder, keyword, category);
    } catch (error) {
      return rejectWithValue(error.message || 'Failed to fetch properties');
    }
  }
);

export const fetchPropertiesByCategory = createAsyncThunk(
  'properties/fetchByCategory',
  async ({ categoryId, pageNumber, pageSize, sortBy, sortOrder }, { rejectWithValue }) => {
    try {
      return await propertyService.getPropertiesByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
    } catch (error) {
      return rejectWithValue(error.message || 'Failed to fetch properties by category');
    }
  }
);

export const searchPropertiesByKeyword = createAsyncThunk(
  'properties/searchByKeyword',
  async ({ keyword, pageNumber, pageSize, sortBy, sortOrder, signal }, { rejectWithValue, signal: thunkSignal }) => {
    try {
      // Create a signal that combines the optional passed signal and the thunk signal
      const combinedSignal = signal 
        ? signal 
        : thunkSignal;
      
      return await propertyService.searchPropertiesByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder, combinedSignal);
    } catch (error) {
      // Don't reject aborted requests
      if (error.name === 'AbortError') {
        console.log("Search by keyword aborted");
        return { aborted: true, content: [], pageNumber: 0, totalElements: 0, totalPages: 0, lastPage: true };
      }
      return rejectWithValue(error.message || 'Failed to search properties by keyword');
    }
  }
);

export const searchPropertiesByFilters = createAsyncThunk(
  'properties/searchByFilters',
  async (filters, { rejectWithValue, signal: thunkSignal }) => {
    try {
      // Add thunk signal to filters if no signal was provided
      const filtersWithSignal = {
        ...filters,
        signal: filters.signal || thunkSignal
      };
      
      const response = await propertyService.searchProperties(filtersWithSignal);
      return response;
    } catch (error) {
      // Don't reject aborted requests
      if (error.name === 'AbortError') {
        console.log("Search by filters aborted");
        return { aborted: true, content: [], pageNumber: 0, totalElements: 0, totalPages: 0, lastPage: true };
      }
      return rejectWithValue(error.response?.data?.message || 'Lỗi khi tìm kiếm bất động sản');
    }
  }
);

export const fetchPropertiesByLocation = createAsyncThunk(
  'properties/fetchByLocation',
  async ({ location, pageNumber, pageSize, sortBy, sortOrder }, { rejectWithValue }) => {
    try {
      return await propertyService.getPropertiesByLocation(location, pageNumber, pageSize, sortBy, sortOrder);
    } catch (error) {
      return rejectWithValue(error.message || 'Failed to fetch properties by location');
    }
  }
);

export const addProperty = createAsyncThunk(
  'properties/add',
  async ({ categoryId, propertyData }, { rejectWithValue }) => {
    try {
      return await propertyService.addProperty(categoryId, propertyData);
    } catch (error) {
      return rejectWithValue(error.message || 'Failed to add property');
    }
  }
);

export const getPropertyById = createAsyncThunk(
  'properties/getById',
  async (id, { rejectWithValue }) => {
    try {
      const response = await propertyService.getPropertyById(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Lỗi khi lấy thông tin bất động sản');
    }
  }
);

export const getPropertiesBySellerId = createAsyncThunk(
  'properties/getPropertiesBySellerId',
  async (sellerId, { rejectWithValue }) => {
    try {
      const response = await propertyService.getPropertiesBySellerId(sellerId);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Lỗi khi lấy danh sách bất động sản của người bán');
    }
  }
);

export const createProperty = createAsyncThunk(
  'properties/createProperty',
  async ({ propertyData, images }, { rejectWithValue }) => {
    try {
      const response = await propertyService.createProperty(propertyData, images);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Lỗi khi tạo tin đăng bất động sản');
    }
  }
);

export const updateProperty = createAsyncThunk(
  'properties/updateProperty',
  async ({ id, propertyData, images }, { rejectWithValue }) => {
    try {
      const response = await propertyService.updateProperty(id, propertyData, images);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Lỗi khi cập nhật tin đăng bất động sản');
    }
  }
);

export const deleteProperty = createAsyncThunk(
  'properties/deleteProperty',
  async (id, { rejectWithValue }) => {
    try {
      await propertyService.deleteProperty(id);
      return id;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Lỗi khi xóa tin đăng bất động sản');
    }
  }
);

export const updatePropertyImages = createAsyncThunk(
  'properties/updateImages',
  async ({ propertyId, images }, { rejectWithValue }) => {
    try {
      return await propertyService.updatePropertyImages(propertyId, images);
    } catch (error) {
      return rejectWithValue(error.message || 'Failed to update property images');
    }
  }
);

const propertySlice = createSlice({
  name: 'properties',
  initialState,
  reducers: {
    clearPropertyError: (state) => {
      state.error = null;
    },
    clearSelectedProperty: (state) => {
      state.selectedProperty = null;
    },
    setSelectedProperty: (state, action) => {
      state.selectedProperty = action.payload;
    },
    clearPropertySuccess: (state) => {
      state.success = false;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch all properties
      .addCase(fetchProperties.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProperties.fulfilled, (state, action) => {
        state.loading = false;
        state.properties = action.payload.content;
        state.pagination = {
          pageNumber: action.payload.pageNumber,
          pageSize: action.payload.pageSize,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
          lastPage: action.payload.lastPage
        };
      })
      .addCase(fetchProperties.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Fetch properties by category
      .addCase(fetchPropertiesByCategory.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchPropertiesByCategory.fulfilled, (state, action) => {
        state.loading = false;
        state.properties = action.payload.content;
        state.pagination = {
          pageNumber: action.payload.pageNumber,
          pageSize: action.payload.pageSize,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
          lastPage: action.payload.lastPage
        };
      })
      .addCase(fetchPropertiesByCategory.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Search properties by keyword
      .addCase(searchPropertiesByKeyword.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchPropertiesByKeyword.fulfilled, (state, action) => {
        // Skip updating state if request was aborted
        if (action.payload.aborted) {
          state.loading = false;
          return;
        }
        
        state.loading = false;
        state.properties = action.payload.content;
        state.pagination = {
          pageNumber: action.payload.pageNumber,
          pageSize: action.payload.pageSize,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
          lastPage: action.payload.lastPage
        };
      })
      .addCase(searchPropertiesByKeyword.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Search properties by filters
      .addCase(searchPropertiesByFilters.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchPropertiesByFilters.fulfilled, (state, action) => {
        // Skip updating state if request was aborted
        if (action.payload.aborted) {
          state.loading = false;
          return;
        }
        
        const { content, pageNumber, pageSize, totalElements, totalPages, lastPage } = action.payload;
        
        if (pageNumber === 0) {
          state.properties = content;
        } else {
          state.properties = [...state.properties, ...content];
        }
        
        state.pagination = {
          pageNumber,
          pageSize,
          totalElements,
          totalPages,
          lastPage
        };
        
        state.loading = false;
      })
      .addCase(searchPropertiesByFilters.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Fetch properties by location
      .addCase(fetchPropertiesByLocation.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchPropertiesByLocation.fulfilled, (state, action) => {
        state.loading = false;
        state.properties = action.payload.content;
        state.pagination = {
          pageNumber: action.payload.pageNumber,
          pageSize: action.payload.pageSize,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
          lastPage: action.payload.lastPage
        };
      })
      .addCase(fetchPropertiesByLocation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Add property
      .addCase(addProperty.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addProperty.fulfilled, (state, action) => {
        state.loading = false;
        state.properties = [action.payload, ...state.properties];
      })
      .addCase(addProperty.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get property by ID
      .addCase(getPropertyById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getPropertyById.fulfilled, (state, action) => {
        state.selectedProperty = action.payload;
        state.loading = false;
      })
      .addCase(getPropertyById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get properties by seller ID
      .addCase(getPropertiesBySellerId.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getPropertiesBySellerId.fulfilled, (state, action) => {
        state.loading = false;
        state.sellerProperties = action.payload;
      })
      .addCase(getPropertiesBySellerId.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Create property
      .addCase(createProperty.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.success = false;
      })
      .addCase(createProperty.fulfilled, (state, action) => {
        state.loading = false;
        state.success = true;
        // Add the new property to seller properties if it exists
        if (state.sellerProperties) {
          state.sellerProperties.push(action.payload);
        }
      })
      .addCase(createProperty.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.success = false;
      })

      // Update property
      .addCase(updateProperty.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.success = false;
      })
      .addCase(updateProperty.fulfilled, (state, action) => {
        state.loading = false;
        state.success = true;
        
        // Update the property in all relevant state arrays
        if (state.sellerProperties) {
          state.sellerProperties = state.sellerProperties.map(property => 
            property.id === action.payload.id ? action.payload : property
          );
        }
        
        if (state.properties) {
          state.properties = state.properties.map(property => 
            property.id === action.payload.id ? action.payload : property
          );
        }
        
        if (state.selectedProperty && state.selectedProperty.id === action.payload.id) {
          state.selectedProperty = action.payload;
        }
      })
      .addCase(updateProperty.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.success = false;
      })

      // Delete property
      .addCase(deleteProperty.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteProperty.fulfilled, (state, action) => {
        state.loading = false;
        
        // Remove the property from all relevant state arrays
        if (state.sellerProperties) {
          state.sellerProperties = state.sellerProperties.filter(
            property => property.id !== action.payload
          );
        }
        
        if (state.properties) {
          state.properties = state.properties.filter(
            property => property.id !== action.payload
          );
        }
        
        if (state.selectedProperty && state.selectedProperty.id === action.payload) {
          state.selectedProperty = null;
        }
      })
      .addCase(deleteProperty.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Update property images
      .addCase(updatePropertyImages.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updatePropertyImages.fulfilled, (state, action) => {
        state.loading = false;
        state.properties = state.properties.map(property => 
          property.id === action.payload.id ? action.payload : property
        );
      })
      .addCase(updatePropertyImages.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { clearPropertyError, clearSelectedProperty, setSelectedProperty, clearPropertySuccess } = propertySlice.actions;

export default propertySlice.reducer; 