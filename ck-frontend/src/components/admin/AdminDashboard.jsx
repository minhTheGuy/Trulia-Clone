import { useState } from 'react';
import { useSelector } from 'react-redux';
import { Navigate } from 'react-router-dom';
import UserManagement from './UserManagement';
import ArticleManagement from './ArticleManagement';
import RevenueManagement from './RevenueManagement';

const AdminDashboard = () => {
  const { isLoggedIn, user } = useSelector((state) => state.auth);
  const [activeTab, setActiveTab] = useState('users');

  // Kiểm tra người dùng đã đăng nhập và có vai trò ADMIN
  if (!isLoggedIn) {
    return <Navigate to="/signin" />;
  }

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');
  if (!isAdmin) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full">
          <div className="text-center">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <h3 className="mt-2 text-lg font-medium text-gray-900">Không có quyền truy cập</h3>
            <p className="mt-1 text-sm text-gray-500">
              Bạn cần có vai trò quản trị viên để truy cập trang này.
            </p>
          </div>
        </div>
      </div>
    );
  }

  const tabs = [
    { id: 'users', label: 'Quản lý người dùng' },
    { id: 'articles', label: 'Quản lý bài viết' },
    { id: 'revenue', label: 'Thống kê doanh thu' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="bg-white shadow overflow-hidden sm:rounded-lg">
          {/* Dashboard header */}
          <div className="px-4 py-5 sm:px-6 bg-blue-600">
            <div className="flex items-center">
              <div className="w-16 h-16 rounded-full bg-white text-blue-600 flex items-center justify-center text-2xl font-bold mr-4">
                {user?.username?.charAt(0).toUpperCase() || 'A'}
              </div>
              <div>
                <h3 className="text-xl leading-6 font-bold text-white">
                  Trang quản trị hệ thống
                </h3>
                <p className="mt-1 max-w-2xl text-sm text-blue-100">
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
                  onClick={() => setActiveTab(tab.id)}
                  className={`${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
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
            {activeTab === 'users' && <UserManagement />}
            {activeTab === 'articles' && <ArticleManagement />}
            {activeTab === 'revenue' && <RevenueManagement />}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard; 