import { useState, useEffect } from 'react';
import adminService from '../../services/adminService';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [modalAction, setModalAction] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 10;

  // Load users on component mount
  useEffect(() => {
    fetchUsers();
  }, [currentPage]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await adminService.getAllUsers(currentPage, pageSize);
      setUsers(response.content || []);
      setTotalPages(response.totalPages || 1);
      setLoading(false);
    } catch (err) {
      setError(err.message || 'Có lỗi xảy ra khi tải danh sách người dùng');
      setLoading(false);
    }
  };

  // Hàm chuyển đổi vai trò từ ROLE_X sang tên hiển thị
  const formatRole = (role) => {
    switch (role) {
      case 'ROLE_USER':
        return { label: 'Người dùng', color: 'bg-gray-100 text-gray-800' };
      case 'ROLE_SELLER':
        return { label: 'Người bán', color: 'bg-green-100 text-green-800' };
      case 'ROLE_BROKER':
        return { label: 'Môi giới', color: 'bg-blue-100 text-blue-800' };
      case 'ROLE_ADMIN':
        return { label: 'Quản trị viên', color: 'bg-purple-100 text-purple-800' };
      default:
        return { label: role, color: 'bg-gray-100 text-gray-800' };
    }
  };

  // Mở modal xác nhận hành động
  const openConfirmModal = (user, action) => {
    setSelectedUser(user);
    setModalAction(action);
    setShowModal(true);
  };

  // Xử lý khóa/mở khóa tài khoản
  const handleToggleStatus = async () => {
    if (!selectedUser) return;

    try {
      const newStatus = !selectedUser.active;
      await adminService.updateUserStatus(selectedUser.id, newStatus);
      
      // Cập nhật state
      setUsers(users.map(user => 
        user.id === selectedUser.id ? { ...user, active: newStatus } : user
      ));

      setShowModal(false);
      setSelectedUser(null);
    } catch (err) {
      setError(err.message || 'Có lỗi xảy ra khi cập nhật trạng thái người dùng');
    }
  };

  // Xử lý thêm/xóa vai trò seller
  const handleToggleSellerRole = async () => {
    if (!selectedUser) return;

    try {
      const hasSeller = selectedUser.roles.includes('ROLE_SELLER');
      
      // Call the API to update the seller role
      await adminService.updateSellerRole(selectedUser.id, !hasSeller);
      
      // Determine new roles after the update
      let newRoles;
      if (hasSeller) {
        // Xóa vai trò seller
        newRoles = selectedUser.roles.filter(role => role !== 'ROLE_SELLER');
      } else {
        // Thêm vai trò seller
        newRoles = [...selectedUser.roles, 'ROLE_SELLER'];
      }
      
      // Cập nhật state
      setUsers(users.map(user => 
        user.id === selectedUser.id ? { ...user, roles: newRoles } : user
      ));

      setShowModal(false);
      setSelectedUser(null);
    } catch (err) {
      setError(err.message || 'Có lỗi xảy ra khi cập nhật vai trò người dùng');
    }
  };

  // Xử lý thêm/xóa vai trò broker
  const handleToggleBrokerRole = async () => {
    if (!selectedUser) return;

    try {
      const hasBroker = selectedUser.roles.includes('ROLE_BROKER');
      
      // Call the API to update the broker role
      await adminService.updateBrokerRole(selectedUser.id, !hasBroker);
      
      // Determine new roles after the update
      let newRoles;
      if (hasBroker) {
        // Xóa vai trò broker
        newRoles = selectedUser.roles.filter(role => role !== 'ROLE_BROKER');
      } else {
        // Thêm vai trò broker
        newRoles = [...selectedUser.roles, 'ROLE_BROKER'];
      }
      
      // Cập nhật state
      setUsers(users.map(user => 
        user.id === selectedUser.id ? { ...user, roles: newRoles } : user
      ));

      setShowModal(false);
      setSelectedUser(null);
    } catch (err) {
      setError(err.message || 'Có lỗi xảy ra khi cập nhật vai trò người dùng');
    }
  };

  // Xử lý hoàn thành hành động
  const handleConfirmAction = () => {
    if (modalAction === 'toggle-status') {
      handleToggleStatus();
    } else if (modalAction === 'toggle-seller') {
      handleToggleSellerRole();
    } else if (modalAction === 'toggle-broker') {
      handleToggleBrokerRole();
    }
  };

  // Lọc dữ liệu dựa trên tìm kiếm và bộ lọc
  const filteredUsers = users.filter(user => {
    const matchSearch = 
      user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      `${user.firstName} ${user.lastName}`.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchRole = roleFilter === 'all' || 
      (roleFilter === 'admin' && user.roles.includes('ROLE_ADMIN')) ||
      (roleFilter === 'seller' && user.roles.includes('ROLE_SELLER')) ||
      (roleFilter === 'broker' && user.roles.includes('ROLE_BROKER')) ||
      (roleFilter === 'user' && user.roles.length === 1 && user.roles.includes('ROLE_USER'));
    
    const matchStatus = statusFilter === 'all' || 
      (statusFilter === 'active' && user.active) ||
      (statusFilter === 'inactive' && !user.active);
    
    return matchSearch && matchRole && matchStatus;
  });

  // Handle pagination
  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border-l-4 border-red-400 p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center space-y-4 sm:space-y-0">
        <h3 className="text-lg leading-6 font-medium text-gray-900">Quản lý người dùng</h3>
      </div>

      {/* Tìm kiếm và Bộ lọc */}
      <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
        <div className="relative flex-grow">
          <input
            type="text"
            placeholder="Tìm kiếm theo tên, email..."
            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
            <svg className="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
            </svg>
          </div>
        </div>
        <div className="relative w-full sm:w-48">
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
          >
            <option value="all">Tất cả vai trò</option>
            <option value="admin">Quản trị viên</option>
            <option value="seller">Người bán</option>
            <option value="broker">Môi giới</option>
            <option value="user">Người dùng</option>
          </select>
        </div>
        <div className="relative w-full sm:w-48">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="active">Đang hoạt động</option>
            <option value="inactive">Bị khóa</option>
          </select>
        </div>
      </div>

      {/* Bảng danh sách người dùng */}
      <div className="flex flex-col">
        <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
            <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Người dùng
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Vai trò
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Trạng thái
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Ngày đăng ký
                    </th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Thao tác
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredUsers.map((user) => (
                    <tr key={user.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="flex-shrink-0 h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                            <span className="text-xl font-medium text-gray-500">
                              {`${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`}
                            </span>
                          </div>
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">
                              {user.username}
                            </div>
                            <div className="text-sm text-gray-500">
                              {user.email}
                            </div>
                            <div className="text-sm text-gray-500">
                              {user.firstName} {user.lastName}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex flex-wrap gap-1">
                          {user.roles.map((role, index) => {
                            const { label, color } = formatRole(role);
                            return (
                              <span key={index} className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${color}`}>
                                {label}
                              </span>
                            );
                          })}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {user.active ? (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            Đang hoạt động
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                            Đã khóa
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {user.createdAt} 
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        {!user.roles.includes('ROLE_ADMIN') && (
                          <>
                            <button
                              onClick={() => openConfirmModal(user, 'toggle-status')}
                              className="text-indigo-600 hover:text-indigo-900 mr-4"
                            >
                              {user.active ? 'Khóa' : 'Mở khóa'}
                            </button>
                            <button
                              onClick={() => openConfirmModal(user, 'toggle-seller')}
                              className="text-green-600 hover:text-green-900 mr-4"
                            >
                              {user.roles.includes('ROLE_SELLER') ? 'Hủy quyền người bán' : 'Cấp quyền người bán'}
                            </button>
                            <button
                              onClick={() => openConfirmModal(user, 'toggle-broker')}
                              className="text-blue-600 hover:text-blue-900"
                            >
                              {user.roles.includes('ROLE_BROKER') ? 'Hủy quyền môi giới' : 'Cấp quyền môi giới'}
                            </button>
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center mt-6">
          <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
            <button
              onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className={`relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium ${
                currentPage === 0 ? 'text-gray-300 cursor-not-allowed' : 'text-gray-500 hover:bg-gray-50'
              }`}
            >
              <span className="sr-only">Previous</span>
              <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
            </button>
            
            {[...Array(totalPages).keys()].map((page) => (
              <button
                key={page}
                onClick={() => handlePageChange(page)}
                className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                  currentPage === page
                    ? 'z-10 bg-blue-50 border-blue-500 text-blue-600'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                }`}
              >
                {page + 1}
              </button>
            ))}
            
            <button
              onClick={() => handlePageChange(Math.min(totalPages - 1, currentPage + 1))}
              disabled={currentPage === totalPages - 1}
              className={`relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium ${
                currentPage === totalPages - 1 ? 'text-gray-300 cursor-not-allowed' : 'text-gray-500 hover:bg-gray-50'
              }`}
            >
              <span className="sr-only">Next</span>
              <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
              </svg>
            </button>
          </nav>
        </div>
      )}

      {/* Modal xác nhận hành động */}
      {showModal && selectedUser && (
        <div className="fixed z-10 inset-0 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div className="fixed inset-0 transition-opacity" aria-hidden="true">
              <div className="absolute inset-0 bg-gray-500 opacity-75"></div>
            </div>
            <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
              <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                <div className="sm:flex sm:items-start">
                  <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
                    <svg className="h-6 w-6 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                  </div>
                  <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">
                      {modalAction === 'toggle-status' 
                        ? (selectedUser.active ? 'Khóa tài khoản' : 'Mở khóa tài khoản')
                        : modalAction === 'toggle-seller'
                        ? (selectedUser.roles.includes('ROLE_SELLER') ? 'Hủy quyền người bán' : 'Cấp quyền người bán')
                        : (selectedUser.roles.includes('ROLE_BROKER') ? 'Hủy quyền môi giới' : 'Cấp quyền môi giới')
                      }
                    </h3>
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">
                        {modalAction === 'toggle-status' 
                          ? `Bạn có chắc chắn muốn ${selectedUser.active ? 'khóa' : 'mở khóa'} tài khoản của người dùng "${selectedUser.username}"?`
                          : modalAction === 'toggle-seller'
                          ? `Bạn có chắc chắn muốn ${selectedUser.roles.includes('ROLE_SELLER') ? 'hủy quyền người bán' : 'cấp quyền người bán'} cho người dùng "${selectedUser.username}"?`
                          : `Bạn có chắc chắn muốn ${selectedUser.roles.includes('ROLE_BROKER') ? 'hủy quyền môi giới' : 'cấp quyền môi giới'} cho người dùng "${selectedUser.username}"?`
                        }
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={handleConfirmAction}
                >
                  Xác nhận
                </button>
                <button
                  type="button"
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={() => setShowModal(false)}
                >
                  Hủy
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;