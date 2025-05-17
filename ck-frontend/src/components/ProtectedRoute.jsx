import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';

/**
 * Component to protect routes that require authentication
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components to render if user is authenticated
 * @returns {React.ReactElement} The protected route
 */
const ProtectedRoute = ({ children }) => {
  const { user } = useSelector(state => state.auth);
  const location = useLocation();

  if (!user) {
    // Redirect to login page if user is not authenticated
    // Save the current path in state so we can redirect back after login
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  // User is authenticated, render the protected component
  return children;
};

export default ProtectedRoute; 