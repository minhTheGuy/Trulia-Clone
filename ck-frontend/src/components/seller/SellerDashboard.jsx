import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Navigate } from 'react-router-dom';
import PropertyListingTable from './PropertyListingTable';
import PropertyForm from './PropertyForm';
import ScheduledToursList from './ScheduledToursList';
import { getPropertiesBySellerId } from '../../redux/slices/propertySlice';

const SellerDashboard = () => {
  const dispatch = useDispatch();
  const { isLoggedIn, user } = useSelector((state) => state.auth);
  const { sellerProperties, loading } = useSelector((state) => state.properties);
  const [activeTab, setActiveTab] = useState('listings');
  const [editingProperty, setEditingProperty] = useState(null);

  // Fetch seller properties on component mount
  useEffect(() => {
    if (isLoggedIn && user?.roles?.includes('ROLE_SELLER')) {
      if (user?.id) {
        // Nếu có user ID, dùng API với seller ID cụ thể
        console.log("Fetching properties for seller ID:", user.id);
        dispatch(getPropertiesBySellerId(user.id));
      } else {
        // Fallback: Dùng API cũ lấy properties của người dùng hiện tại
        console.log("Fallback: Using default seller properties API");
        dispatch(getPropertiesBySellerId(user.id));
      }
    }
  }, [dispatch, isLoggedIn, user]);

  // Kiểm tra người dùng đã đăng nhập và có vai trò SELLER
  if (!isLoggedIn) {
    return <Navigate to="/signin" />;
  }

  const isSeller = user?.roles?.includes('ROLE_SELLER');
  if (!isSeller) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full">
          <div className="text-center">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <h3 className="mt-2 text-lg font-medium text-gray-900">Không có quyền truy cập</h3>
            <p className="mt-1 text-sm text-gray-500">
              Bạn cần có vai trò môi giới để truy cập trang này.
            </p>
          </div>
        </div>
      </div>
    );
  }

  // Calculate statistics
  const totalListings = sellerProperties?.length || 0;
  const totalViews = sellerProperties?.reduce((total, property) => total + (property.views || 0), 0) || 0;
  const activeListings = sellerProperties?.filter(property => property.status === 'ACTIVE').length || 0;

  const tabs = [
    { id: 'listings', label: 'Danh sách tin đăng' },
    { id: 'tours', label: 'Lịch tham quan' },
    { id: 'create', label: 'Tạo tin mới' },
    { id: 'statistics', label: 'Thống kê' }
  ];

  // Xử lý khi click vào nút chỉnh sửa tin đăng
  const handleEditProperty = (property) => {
    setEditingProperty(property);
    setActiveTab('create');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="bg-white shadow overflow-hidden sm:rounded-lg">
          {/* Dashboard header */}
          <div className="px-4 py-5 sm:px-6 bg-rose-600">
            <div className="flex items-center">
              <div className="w-16 h-16 rounded-full bg-white text-rose-600 flex items-center justify-center text-2xl font-bold mr-4">
                {user?.username?.charAt(0).toUpperCase() || 'S'}
              </div>
              <div>
                <h3 className="text-xl leading-6 font-bold text-white">
                  Trang quản lý dành cho môi giới
                </h3>
                <p className="mt-1 max-w-2xl text-sm text-rose-100">
                  {user?.username} - {user?.email}
                </p>
              </div>
            </div>
          </div>

          {/* Tabs */}
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex" aria-label="Tabs">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => {
                    setActiveTab(tab.id);
                    if (tab.id === 'create' && editingProperty === null) {
                      setEditingProperty(null); // Reset editing state when switching to create
                    }
                  }}
                  className={`${
                    activeTab === tab.id
                      ? 'border-rose-500 text-rose-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  } whitespace-nowrap py-4 px-6 border-b-2 font-medium text-sm`}
                  aria-current={activeTab === tab.id ? 'page' : undefined}
                >
                  {tab.label}
                </button>
              ))}
            </nav>
          </div>

          {/* Tab content */}
          <div className="px-4 py-5 sm:p-6">
            {activeTab === 'listings' && (
              <PropertyListingTable onEditProperty={handleEditProperty} />
            )}

            {activeTab === 'tours' && (
              <ScheduledToursList />
            )}

            {activeTab === 'create' && (
              <PropertyForm propertyData={editingProperty} />
            )}

            {activeTab === 'statistics' && (
              <div className="space-y-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Thống kê hoạt động</h3>
                
                <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
                  {/* Card Tổng số tin đăng */}
                  <div className="bg-white overflow-hidden shadow rounded-lg">
                    <div className="px-4 py-5 sm:p-6">
                      <dt className="text-sm font-medium text-gray-500 truncate">
                        Tổng số tin đăng
                      </dt>
                      <dd className="mt-1 text-3xl font-semibold text-gray-900">
                        {loading ? (
                          <div className="h-8 w-16 bg-gray-200 animate-pulse rounded"></div>
                        ) : (
                          totalListings
                        )}
                      </dd>
                    </div>
                    <div className="bg-gray-50 px-4 py-4 sm:px-6">
                      <div className="text-sm">
                        <button
                          onClick={() => setActiveTab('listings')}
                          className="font-medium text-rose-600 hover:text-rose-500"
                        >
                          Xem tất cả
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* Card Lượt xem */}
                  <div className="bg-white overflow-hidden shadow rounded-lg">
                    <div className="px-4 py-5 sm:p-6">
                      <dt className="text-sm font-medium text-gray-500 truncate">
                        Tổng lượt xem
                      </dt>
                      <dd className="mt-1 text-3xl font-semibold text-gray-900">
                        {loading ? (
                          <div className="h-8 w-16 bg-gray-200 animate-pulse rounded"></div>
                        ) : (
                          totalViews
                        )}
                      </dd>
                    </div>
                    <div className="bg-gray-50 px-4 py-4 sm:px-6">
                      <div className="text-sm">
                        <button
                          onClick={() => setActiveTab('listings')}
                          className="font-medium text-rose-600 hover:text-rose-500"
                        >
                          Xem chi tiết
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* Card tin đăng đang hiển thị */}
                  <div className="bg-white overflow-hidden shadow rounded-lg">
                    <div className="px-4 py-5 sm:p-6">
                      <dt className="text-sm font-medium text-gray-500 truncate">
                        Tin đăng đang hiển thị
                      </dt>
                      <dd className="mt-1 text-3xl font-semibold text-gray-900">
                        {loading ? (
                          <div className="h-8 w-16 bg-gray-200 animate-pulse rounded"></div>
                        ) : (
                          activeListings
                        )}
                      </dd>
                    </div>
                    <div className="bg-gray-50 px-4 py-4 sm:px-6">
                      <div className="text-sm">
                        <button
                          onClick={() => {
                            setActiveTab('listings');
                          }}
                          className="font-medium text-rose-600 hover:text-rose-500"
                        >
                          Xem danh sách
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                {totalListings === 0 && !loading && (
                  <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mt-6">
                    <div className="flex">
                      <div className="flex-shrink-0">
                        <svg className="h-5 w-5 text-yellow-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                        </svg>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm text-yellow-700">
                          Bạn chưa có tin đăng nào. Hãy tạo tin đăng mới bằng cách chọn tab "Tạo tin mới".
                        </p>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SellerDashboard; 