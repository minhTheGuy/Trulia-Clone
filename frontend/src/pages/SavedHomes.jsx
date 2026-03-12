import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import PropertyCard from '../components/PropertyCard';
import { fetchSavedHomes } from '../redux/slices/savedHomesSlice'; // Updated import source

const SavedHomes = () => {
  const dispatch = useDispatch();
  const { savedHomes, loading, error } = useSelector(state => state.savedHomes); // Updated to use savedHomes state
  const { user } = useSelector(state => state.auth);

  useEffect(() => {
    if (user && user.id) {
      dispatch(fetchSavedHomes(user.id)); // Fetch saved homes for the logged-in user
    }
  }, [dispatch, user]);

  if (!user) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center">
        <h2 className="text-2xl font-semibold text-gray-800 mb-4">Please Log In</h2>
        <p className="text-gray-600 mb-6">You need to be logged in to view your saved homes.</p>
        <Link 
          to="/login"
          className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
        >
          Log In
        </Link>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-rose-500 mx-auto"></div>
        <p className="mt-4 text-gray-600">Loading your saved homes...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center">
        <h2 className="text-2xl font-semibold text-red-600 mb-4">Error</h2>
        <p className="text-gray-600">Could not load saved homes: {error}</p>
      </div>
    );
  }

  if (!savedHomes || savedHomes.length === 0) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center">
        <h2 className="text-2xl font-semibold text-gray-800 mb-4">No Saved Homes</h2>
        <p className="text-gray-600 mb-6">You haven't saved any homes yet. Start exploring and save your favorites!</p>
        <Link 
          to="/"
          className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
        >
          Find Homes
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Your Saved Homes</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {savedHomes.map(property => (
          <PropertyCard key={property.id} property={property} />
        ))}
      </div>
    </div>
  );
};

export default SavedHomes; 