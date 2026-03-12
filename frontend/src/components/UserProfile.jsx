import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Navigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import authService from '../services/authService';
import PropertyCard from './PropertyCard';
import { fetchSavedHomes } from '../redux/slices/savedHomesSlice';

const UserProfile = () => {
  const { isLoggedIn, user } = useSelector((state) => state.auth);
  const { savedHomes, loading: loadingSavedHomes, error: savedHomesError } = useSelector(state => state.savedHomes);
  const [activeTab, setActiveTab] = useState('profile');
  
  const dispatch = useDispatch();
  
  // State for password change form
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Function to fetch saved properties when favorites tab is activated
  useEffect(() => {
    if (activeTab === 'favorites' && user && user.id) {
      dispatch(fetchSavedHomes(user.id));
    }
  }, [activeTab, dispatch, user]);

  // Xử lý thay đổi input trong form đổi mật khẩu
  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordForm({
      ...passwordForm,
      [name]: value
    });
    
    // Clear error khi người dùng sửa input
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: ''
      });
    }
  };

  // Validate form trước khi submit
  const validatePasswordForm = () => {
    const newErrors = {};
    
    if (!passwordForm.currentPassword) {
      newErrors.currentPassword = 'Vui lòng nhập mật khẩu hiện tại';
    }
    
    if (!passwordForm.newPassword) {
      newErrors.newPassword = 'Vui lòng nhập mật khẩu mới';
    } else if (passwordForm.newPassword.length < 6) {
      newErrors.newPassword = 'Mật khẩu phải có ít nhất 6 ký tự';
    }
    
    if (!passwordForm.confirmPassword) {
      newErrors.confirmPassword = 'Vui lòng xác nhận mật khẩu mới';
    } else if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      newErrors.confirmPassword = 'Mật khẩu xác nhận không khớp';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Xử lý submit form đổi mật khẩu
  const handleSubmitPasswordChange = async (e) => {
    e.preventDefault();
    
    if (!validatePasswordForm()) {
      return;
    }
    
    setLoading(true);
    
    try {
      await authService.changePassword(
        passwordForm.currentPassword,
        passwordForm.newPassword
      );
      
      // Reset form sau khi đổi mật khẩu thành công
      setPasswordForm({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
      
      toast.success('Đổi mật khẩu thành công!');
    } catch (error) {
      const message = error.response?.data?.message || 'Đã xảy ra lỗi khi đổi mật khẩu.';
      toast.error(message);
      
      // Nếu là lỗi mật khẩu không đúng
      if (error.response?.status === 401) {
        setErrors({
          ...errors,
          currentPassword: 'Mật khẩu hiện tại không đúng'
        });
      }
    } finally {
      setLoading(false);
    }
  };

  // Redirect to login if not logged in
  if (!isLoggedIn) {
    return <Navigate to="/signin" />;
  }

  const tabs = [
    { id: 'profile', label: 'Thông tin tài khoản' },
    { id: 'favorites', label: 'Tin đã lưu' },
    { id: 'settings', label: 'Cài đặt' },
  ];

  // Hàm chuyển đổi vai trò từ ROLE_X sang tên hiển thị
  const formatRole = (role) => {
    switch(role) {
      case 'ROLE_USER':
        return 'Người dùng';
      case 'ROLE_SELLER':
        return 'Môi giới';
      case 'ROLE_ADMIN':
        return 'Quản trị viên';
      default:
        return role;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="bg-white shadow overflow-hidden sm:rounded-lg">
          {/* Profile header */}
          <div className="px-4 py-5 sm:px-6 bg-rose-600">
            <div className="flex items-center">
              <div className="w-16 h-16 rounded-full bg-white text-rose-600 flex items-center justify-center text-2xl font-bold mr-4">
                {user?.username?.charAt(0).toUpperCase() || 'U'}
              </div>
              <div>
                <h3 className="text-xl leading-6 font-bold text-white">
                  {user?.username || 'User'}
                </h3>
                <p className="mt-1 max-w-2xl text-sm text-rose-100">
                  {user?.email || 'email@example.com'}
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
                  onClick={() => setActiveTab(tab.id)}
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
            {activeTab === 'profile' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg leading-6 font-medium text-gray-900">Thông tin cá nhân</h3>
                  <div className="mt-5 border-t border-gray-200">
                    <dl className="divide-y divide-gray-200">
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4">
                        <dt className="text-sm font-medium text-gray-500">Tên đăng nhập</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{user?.username}</dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4">
                        <dt className="text-sm font-medium text-gray-500">Email</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{user?.email}</dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4">
                        <dt className="text-sm font-medium text-gray-500">Họ tên</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                          {user?.firstName} {user?.lastName || 'Chưa cập nhật'}
                        </dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4">
                        <dt className="text-sm font-medium text-gray-500">Số điện thoại</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                          {user?.phoneNumber || 'Chưa cập nhật'}
                        </dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4">
                        <dt className="text-sm font-medium text-gray-500">Vai trò</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                          {user?.roles ? user.roles.map(formatRole).join(', ') : 'Người dùng'}
                        </dd>
                      </div>
                    </dl>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'favorites' && (
              <div className="space-y-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Bất động sản đã lưu</h3>
                {loadingSavedHomes ? (
                  <div className="flex justify-center py-8">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-rose-600"></div>
                  </div>
                ) : savedHomesError ? (
                  <div className="rounded-md bg-red-50 p-4 my-4">
                    <div className="flex">
                      <div className="ml-3">
                        <h3 className="text-sm font-medium text-red-800">Lỗi</h3>
                        <div className="mt-2 text-sm text-red-700">
                          <p>{savedHomesError}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                ) : savedHomes.length === 0 ? (
                  <div className="text-center py-8">
                    <svg xmlns="http://www.w3.org/2000/svg" className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                    <h3 className="mt-2 text-sm font-medium text-gray-900">Chưa có bất động sản nào được lưu</h3>
                    <p className="mt-1 text-sm text-gray-500">
                      Bạn chưa lưu bất động sản nào. Duyệt qua các bất động sản và nhấn vào biểu tượng trái tim để lưu.
                    </p>
                    <div className="mt-6">
                      <Link
                        to="/"
                        className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
                      >
                        Duyệt bất động sản
                      </Link>
                    </div>
                  </div>
                ) : (
                  <div className="mt-5 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {savedHomes.map((property) => (
                      <PropertyCard key={property.id} property={property} />
                    ))}
                  </div>
                )}
              </div>
            )}

            {activeTab === 'settings' && (
              <div className="space-y-6">
                <h3 className="text-lg leading-6 font-medium text-gray-900">Cài đặt tài khoản</h3>
                <div className="mt-5">
                  <form className="space-y-6" onSubmit={handleSubmitPasswordChange}>
                    <div>
                      <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700">
                        Mật khẩu hiện tại
                      </label>
                      <div className="mt-1">
                        <input
                          id="currentPassword"
                          name="currentPassword"
                          type="password"
                          value={passwordForm.currentPassword}
                          onChange={handlePasswordChange}
                          required
                          className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                        />
                        {errors.currentPassword && (
                          <p className="mt-1 text-sm text-red-600">{errors.currentPassword}</p>
                        )}
                      </div>
                    </div>

                    <div>
                      <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">
                        Mật khẩu mới
                      </label>
                      <div className="mt-1">
                        <input
                          id="newPassword"
                          name="newPassword"
                          type="password"
                          value={passwordForm.newPassword}
                          onChange={handlePasswordChange}
                          required
                          className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                        />
                        {errors.newPassword && (
                          <p className="mt-1 text-sm text-red-600">{errors.newPassword}</p>
                        )}
                      </div>
                    </div>

                    <div>
                      <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                        Xác nhận mật khẩu mới
                      </label>
                      <div className="mt-1">
                        <input
                          id="confirmPassword"
                          name="confirmPassword"
                          type="password"
                          value={passwordForm.confirmPassword}
                          onChange={handlePasswordChange}
                          required
                          className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                        />
                        {errors.confirmPassword && (
                          <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>
                        )}
                      </div>
                    </div>

                    <div>
                      <button
                        type="submit"
                        disabled={loading}
                        className={`w-full inline-flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white ${
                          loading 
                            ? 'bg-rose-300 cursor-not-allowed' 
                            : 'bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500'
                        }`}
                      >
                        {loading ? 'Đang xử lý...' : 'Cập nhật mật khẩu'}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserProfile; 