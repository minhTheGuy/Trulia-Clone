import { useState, useEffect, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchProperties } from '../redux/slices/propertySlice'
import PropertyCard from './PropertyCard'

const PropertyList = ({ filters, searchKeyword, initialError = false, initialFetch = false }) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [sortOrder, setSortOrder] = useState('asc');
  const [sortBy, setSortBy] = useState('price');
  const [hasError, setHasError] = useState(initialError);
  const [isFetching, setIsFetching] = useState(false);
  const [filteredProperties, setFilteredProperties] = useState([]);
  
  // Use refs to store mutable values that don't trigger re-renders
  const isFirstRenderRef = useRef(true);
  
  const { properties, loading, error } = useSelector(state => state.properties);

  // Function to filter properties based on current filters and search keyword
  const filterProperties = useCallback(() => {
    if (!properties) return [];

    return properties.filter(property => {
      // Filter by search keyword
      if (searchKeyword) {
        const searchLower = searchKeyword.toLowerCase();
        const matchesKeyword = 
          property.title?.toLowerCase().includes(searchLower) ||
          property.description?.toLowerCase().includes(searchLower) ||
          property.address?.toLowerCase().includes(searchLower);
        
        if (!matchesKeyword) return false;
      }

      // Filter by property type
      if (filters?.type && filters.type !== 'all' && property.type !== filters.type) {
        return false;
      }

      // Filter by price range
      if (filters?.priceRange) {
        const [minPrice, maxPrice] = filters.priceRange;
        if (property.price < minPrice || property.price > maxPrice) {
          return false;
        }
      }

      // Filter by bedrooms
      if (filters?.bedrooms && filters.bedrooms !== 'any') {
        const minBedrooms = parseInt(filters.bedrooms);
        if (property.bedrooms < minBedrooms) {
          return false;
        }
      }

      // Filter by bathrooms
      if (filters?.bathrooms && filters.bathrooms !== 'any') {
        const minBathrooms = parseFloat(filters.bathrooms);
        if (property.bathrooms < minBathrooms) {
          return false;
        }
      }

      // Filter by square footage
      if (filters?.sqftRange) {
        const [minSqft, maxSqft] = filters.sqftRange;
        if (property.squareFootage < minSqft || property.squareFootage > maxSqft) {
          return false;
        }
      }

      // Filter by home types
      if (filters?.homeTypes?.length > 0) {
        // Check if the property type matches any of the selected home types
        // Map between property.type (English) and homeTypes (Vietnamese)
        // This mapping should match the homeTypeOptions in FilterBar
        const typeToHomeTypeMap = {
          'house': 'Nhà riêng',
          'villa': 'Biệt thự',
          'townhouse': 'Nhà mặt phố',
          'apartment': 'Căn hộ cao cấp',
          'condo': 'Studio',
          // Add any additional mappings as needed
        };
        
        // Get the Vietnamese home type equivalent or use property.type directly
        let propertyHomeType = typeToHomeTypeMap[property.type] || '';
        
        // Also check if the property.homeType field exists and matches directly
        if (property.homeType && typeof property.homeType === 'string') {
          propertyHomeType = property.homeType;
        }
        
        console.log('Filtering by homeTypes:', { 
          propertyId: property.id,
          propertyType: property.type, 
          mappedHomeType: propertyHomeType, 
          selectedHomeTypes: filters.homeTypes,
          isMatch: filters.homeTypes.includes(propertyHomeType)
        });
        
        if (!filters.homeTypes.includes(propertyHomeType)) {
          return false;
        }
      }

      // Filter by features
      if (filters?.features?.length > 0) {
        // Get property features - could be in different formats
        const propertyFeatures = Array.isArray(property.features) 
          ? property.features 
          : (property.amenities && Array.isArray(property.amenities) 
              ? property.amenities 
              : []);
        
        console.log('Filtering by features:', { 
          propertyId: property.id,
          propertyFeatures: propertyFeatures, 
          selectedFeatures: filters.features,
          hasFeatures: propertyFeatures.length > 0
        });
        
        if (propertyFeatures.length === 0) {
          return false;
        }
        
        // Check if property has all the selected features
        const hasAllFeatures = filters.features.every(feature => {
          // Convert both to lowercase for case-insensitive comparison
          const hasFeature = propertyFeatures.some(propFeature => 
            typeof propFeature === 'string' && 
            propFeature.toLowerCase().includes(feature.toLowerCase())
          );
          
          console.log(`Checking if property ${property.id} has feature ${feature}: ${hasFeature}`);
          return hasFeature;
        });
        
        if (!hasAllFeatures) {
          return false;
        }
      }

      return true;
    });
  }, [properties, filters, searchKeyword]);

  // Function to sort properties
  const sortProperties = useCallback((propertiesToSort) => {
    return [...propertiesToSort].sort((a, b) => {
      if (sortBy === 'price') {
        return sortOrder === 'asc' ? a.price - b.price : b.price - a.price;
      } else if (sortBy === 'yearBuilt') {
        return sortOrder === 'asc' ? a.yearBuilt - b.yearBuilt : b.yearBuilt - a.yearBuilt;
      }
      return 0;
    });
  }, [sortBy, sortOrder]);

  // Effect to update filtered and sorted properties when dependencies change
  useEffect(() => {
    const filtered = filterProperties();
    const sorted = sortProperties(filtered);
    setFilteredProperties(sorted);
  }, [filterProperties, sortProperties]);

  // Function to fetch all properties on initial load
  const fetchAllProperties = useCallback(async () => {
    if (isFetching) return;
    
    setIsFetching(true);
    
    try {
      await dispatch(fetchProperties({
        pageNumber: 0,
        pageSize: 1000, // Fetch all properties at once since we're filtering locally
        sortBy,
        sortOrder
      })).unwrap();
    } catch (err) {
      console.error("Error fetching properties:", err);
      setHasError(true);
    } finally {
      setIsFetching(false);
    }
  }, [dispatch, sortBy, sortOrder, isFetching]);

  // Effect for initial fetch when the component mounts
  useEffect(() => {
    if (initialFetch && isFirstRenderRef.current) {
      fetchAllProperties();
    }
    isFirstRenderRef.current = false;
  }, [initialFetch, fetchAllProperties]);

  // Handle sort change
  const handleSortChange = useCallback((e) => {
    const value = e.target.value;
    
    switch(value) {
      case 'price_high_low':
        setSortBy('price');
        setSortOrder('desc');
        break;
      case 'price_low_high':
        setSortBy('price');
        setSortOrder('asc');
        break;
      case 'newest':
        setSortBy('yearBuilt');
        setSortOrder('desc');
        break;
      default:
        setSortBy('price');
        setSortOrder('asc');
    }
  }, []);

  // Show loading state only during initial load
  if (loading && properties.length === 0 && !error && !hasError) {
    return (
      <div className="text-center py-16">
        <div className="flex justify-center mb-4">
          <svg className="animate-spin h-8 w-8 text-rose-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900">Đang tải dữ liệu...</h3>
      </div>
    );
  }

  // Show error state
  if ((error || hasError) && properties.length === 0) {
    return (
      <div className="text-center py-16">
        <div className="bg-red-50 border-l-4 border-red-400 p-4 max-w-lg mx-auto mb-6">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Lỗi khi tải dữ liệu</h3>
              <p className="text-sm text-red-700 mt-1">{error || "Không thể kết nối đến máy chủ. Vui lòng thử lại sau."}</p>
            </div>
          </div>
        </div>
        <button 
          onClick={fetchAllProperties} 
          className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700"
        >
          Thử lại
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-900">
          {filteredProperties.length} Bất động sản {searchKeyword && `cho "${searchKeyword}"`}
        </h2>
        <div className="flex items-center">
          <span className="mr-2 text-sm text-gray-700">Sắp xếp theo:</span>
          <select 
            className="text-sm border-gray-300 rounded-md shadow-sm focus:ring-rose-500 focus:border-rose-500"
            onChange={handleSortChange}
            value={sortBy === 'price' ? (sortOrder === 'asc' ? 'price_low_high' : 'price_high_low') : 'newest'}
          >
            <option value="price_low_high">Giá (Thấp đến Cao)</option>
            <option value="price_high_low">Giá (Cao đến Thấp)</option>
            <option value="newest">Mới nhất</option>
          </select>
        </div>
      </div>

      {filteredProperties.length === 0 && !loading && !error && !hasError ? (
        <div className="text-center py-16">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Không tìm thấy bất động sản nào phù hợp</h3>
          <p className="text-gray-500 mb-6">Hãy thử lại với các tiêu chí khác</p>
          <button 
            onClick={() => navigate('/')} 
            className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700"
          >
            Về trang chủ
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {filteredProperties.map(property => (
            <PropertyCard key={property.id} property={property}/>
          ))}
        </div>
      )}
    </div>
  )
}

export default PropertyList 