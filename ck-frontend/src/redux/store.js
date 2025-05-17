import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import propertyReducer from './slices/propertySlice';
import messageReducer from './slices/messageSlice';
import userReducer from './slices/userSlice';
import savedHomesReducer from './slices/savedHomesSlice';
import savedSearchesReducer from './slices/savedSearchesSlice';

const store = configureStore({
  reducer: {
    auth: authReducer,
    properties: propertyReducer,
    message: messageReducer,
    users: userReducer,
    savedHomes: savedHomesReducer,
    savedSearches: savedSearchesReducer,
    // Add other reducers here as needed
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore non-serializable values in specified action types
        ignoredActions: ['auth/login/fulfilled', 'auth/refreshToken/fulfilled'],
      },
    }),
});

export default store; 