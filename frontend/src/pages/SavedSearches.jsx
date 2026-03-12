import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { fetchSavedSearches, deleteSavedSearch } from '../redux/slices/savedSearchesSlice';
import { toast } from 'react-toastify';

const SavedSearches = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector(state => state.auth);
  const { savedSearches, loading, error } = useSelector(state => state.savedSearches);
  const [isDeleting, setIsDeleting] = useState(false);
  
  // Fetch saved searches when component mounts
  useEffect(() => {
    if (user) {
      dispatch(fetchSavedSearches(user.id));
    }
  }, [dispatch, user]);

  // Handle applying a saved search
  const applySearch = (searchCriteria) => {
    // Build the query string from the search criteria
    const queryParams = new URLSearchParams();
    
    if (searchCriteria.keyword) {
      queryParams.append('keyword', searchCriteria.keyword);
    }
    
    // Append other relevant filters
    if (searchCriteria.type && searchCriteria.type !== 'all') {
      queryParams.append('type', searchCriteria.type);
    }
    
    // Navigate to home page with the query parameters
    navigate(`/?${queryParams.toString()}`);
  };

  // Handle deleting a saved search
  const handleDeleteSearch = async (searchId) => {
    if (!user || isDeleting) return;
    
    setIsDeleting(true);
    try {
      await dispatch(deleteSavedSearch({ userId: user.id, searchId })).unwrap();
      toast.success('Đã xóa tìm kiếm thành công');
    } catch (error) {
      toast.error(typeof error === 'string' ? error : 'Không thể xóa tìm kiếm. Vui lòng thử lại sau.');
    } finally {
      setIsDeleting(false);
    }
  };

  // Format date to locale string
  const formatDate = (dateString) => {
    if (!dateString) return '';
    
    try {
      return new Date(dateString).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    } catch (error) {
      return '';
    }
  };
  
  // Format price range to display string
  const formatPriceRange = (criteria) => {
    if (!criteria || !criteria.priceRange) return 'Tất cả mức giá';
    
    const [min, max] = criteria.priceRange;
    
    // Format to VND currency
    const formatCurrency = (value) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        maximumFractionDigits: 0
      }).format(value);
    };
    
    if (min === 0 && max === 100000000000) {
      return 'Tất cả mức giá';
    } else if (min === 0) {
      return `Dưới ${formatCurrency(max)}`;
    } else if (max === 100000000000) {
      return `Trên ${formatCurrency(min)}`;
    } else {
      return `${formatCurrency(min)} - ${formatCurrency(max)}`;
    }
  };
  
  // Get property type string
  const getPropertyType = (criteria) => {
    if (!criteria || !criteria.type || criteria.type === 'all') return 'Tất cả loại bất động sản';
    
    const typeMap = {
      'house': 'Nhà riêng',
      'apartment': 'Chung cư',
      'condo': 'Căn hộ',
      'townhouse': 'Nhà phố',
      'villa': 'Biệt thự',
      'land': 'Đất',
      'commercial': 'Căn hộ kinh doanh',
      'office': 'Văn phòng',
      'warehouse': 'Kho hàng',
      'industrial': 'Nhà máy',
      'hotel': 'Khách sạn',
      'other': 'Khác'
    };
    
    return typeMap[criteria.type] || criteria.type;
  };

  if (loading && savedSearches.length === 0) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Tìm kiếm đã lưu</h1>
        <div className="text-center py-10">
          <div className="animate-spin h-10 w-10 border-4 border-rose-500 rounded-full border-t-transparent mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải tìm kiếm đã lưu...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Tìm kiếm đã lưu</h1>
        <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-red-700">
                {typeof error === 'string' ? error : 'Đã xảy ra lỗi khi tải tìm kiếm đã lưu'}
              </p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Tìm kiếm đã lưu</h1>
      
      {savedSearches.length === 0 ? (
        <div className="text-center py-10 bg-white rounded-lg shadow-sm">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <h3 className="mt-2 text-xl font-semibold text-gray-900">Chưa có tìm kiếm nào được lưu</h3>
          <p className="mt-1 text-sm text-gray-500">Bạn chưa lưu tìm kiếm nào. Khi bạn thực hiện tìm kiếm mà bạn thích, bạn có thể lưu để tham khảo sau.</p>
          <div className="mt-6">
            <Link
              to="/"
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
            >
              <svg className="-ml-1 mr-2 h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
              </svg>
              Bắt đầu tìm kiếm mới
            </Link>
          </div>
        </div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul className="divide-y divide-gray-200">
            {savedSearches.map((search) => (
              <li key={search.id} className="relative hover:bg-gray-50">
                <div className="px-4 py-4 sm:px-6">
                  <div className="flex items-center justify-between">
                    <p className="text-lg font-medium text-rose-600 truncate">
                      {search.name}
                    </p>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => applySearch(search.criteria)}
                        className="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-5 font-medium rounded-md text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
                      >
                        Áp dụng
                      </button>
                      <button
                        onClick={() => handleDeleteSearch(search.id)}
                        disabled={isDeleting}
                        className="inline-flex items-center px-3 py-1 border border-gray-300 text-sm leading-5 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
                      >
                        {isDeleting ? '...' : 'Xóa'}
                      </button>
                    </div>
                  </div>
                  <div className="mt-2 sm:flex sm:justify-between">
                    <div className="sm:flex sm:flex-col">
                      <p className="flex items-center text-sm text-gray-500">
                        {/* Property type */}
                        <svg className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                          <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
                        </svg>
                        {getPropertyType(search.criteria)}
                      </p>
                      <p className="mt-2 flex items-center text-sm text-gray-500">
                        {/* Price range */}
                        <svg className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                          <path d="M8.433 7.418c.155-.103.346-.196.567-.267v1.698a2.5 2.5 0 00-.567-.267C8.07 8.488 8 8.737 8 9.001c0 .264.07.513.433.683a2.5 2.5 0 00.567.266v1.698c-.221-.071-.412-.164-.567-.267-.364-.17-.433-.419-.433-.683s.07-.513.433-.683zm4.567-.267v1.698c.221-.071.412-.164.567-.267.364-.17.433-.419.433-.683s-.07-.513-.433-.683a2.5 2.5 0 00-.567-.267zM9 6a.75.75 0 01.75.75v3.504c.336.112.658.29.933.504a1.5 1.5 0 01.567 1.18c0 .48-.181.94-.567 1.28-.372.344-.833.536-1.333.583v.752a.75.75 0 01-1.5 0v-.746a3.477 3.477 0 01-1.367-.583A1.5 1.5 0 016 11.99c0-.48.18-.938.567-1.28a2.5 2.5 0 011.433-.537V8.25A.75.75 0 019 6z" />
                        </svg>
                        {formatPriceRange(search.criteria)}
                      </p>
                      {search.criteria.keyword && (
                        <p className="mt-2 flex items-center text-sm text-gray-500">
                          {/* Keyword */}
                          <svg className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
                          </svg>
                          Từ khóa: {search.criteria.keyword}
                        </p>
                      )}
                    </div>
                    <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
                      {/* Created date */}
                      <svg className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                      </svg>
                      <p>
                        Đã lưu vào <time dateTime={search.criteria.createdAt}>{formatDate(search.criteria.createdAt)}</time>
                      </p>
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default SavedSearches; 