import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { getPropertiesBySellerId, deleteProperty, updateProperty } from '../../redux/slices/propertySlice';

const PropertyListingTable = ({ onEditProperty }) => {
  const dispatch = useDispatch();
  const { sellerProperties, loading } = useSelector(state => state.properties);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [propertyToDelete, setPropertyToDelete] = useState(null);
  const { user } = useSelector(state => state.auth);

  // Format giá từ số thành chuỗi tiền VNĐ
  const formatPrice = (price) => {
    if (price >= 1000000000) {
      return `${(price / 1000000000).toLocaleString('vi-VN', { maximumFractionDigits: 1 })} tỷ`;
    } else if (price >= 1000000) {
      return `${(price / 1000000).toLocaleString('vi-VN', { maximumFractionDigits: 0 })} triệu`;
    } else {
      return `${price.toLocaleString('vi-VN')} VNĐ`;
    }
  };

  // Tải danh sách bất động sản của người bán
  useEffect(() => {
    dispatch(getPropertiesBySellerId(user.id));
  }, [dispatch, user.id]);

  // Lọc dữ liệu dựa trên tìm kiếm và trạng thái
  const filteredProperties = sellerProperties?.filter(property => {
    const matchSearch = property.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                        property.address?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = filterStatus === 'all' || property.status === filterStatus;
    return matchSearch && matchStatus;
  }) || [];

  // Xử lý khi click vào nút xóa
  const handleDeleteClick = (property) => {
    setPropertyToDelete(property);
    setShowDeleteModal(true);
  };

  // Xử lý khi click vào nút đánh dấu đã bán
  const handleMarkAsSold = (property) => {
    const updatedProperty = {
      ...property,
      status: 'SOLD'
    };
    
    dispatch(updateProperty({ 
      id: property.id, 
      propertyData: updatedProperty 
    }))
      .unwrap()
      .then(() => {
        // Reload the list after successful update
        dispatch(getPropertiesBySellerId(user.id));
      })
      .catch((error) => {
        console.error("Error updating property status:", error);
      });
  };

  // Xác nhận xóa bất động sản
  const confirmDelete = () => {
    if (propertyToDelete) {
      dispatch(deleteProperty(propertyToDelete.id))
        .unwrap()
        .then(() => {
          // Reload the list after successful deletion
          dispatch(getPropertiesBySellerId(user.id));
        })
        .catch((error) => {
          console.error("Error deleting property:", error);
        });
      setShowDeleteModal(false);
      setPropertyToDelete(null);
    }
  };

  const getStatusBadge = (status) => {
    switch(status) {
      case 'ACTIVE':
      case 'active':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">Đang hiển thị</span>;
      case 'PENDING':
      case 'pending':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">Đang duyệt</span>;
      case 'EXPIRED':
      case 'expired':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">Hết hạn</span>;
      case 'SOLD':
      case 'sold':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">Đã bán</span>;
      default:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">{status}</span>;
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-rose-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center space-y-4 sm:space-y-0">
        <h3 className="text-lg leading-6 font-medium text-gray-900">Quản lý tin bất động sản</h3>
        <button
          type="button"
          onClick={() => onEditProperty(null)}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
        >
          <svg className="-ml-1 mr-2 h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clipRule="evenodd" />
          </svg>
          Tạo tin mới
        </button>
      </div>

      {/* Tìm kiếm và Bộ lọc */}
      <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
        <div className="relative flex-grow">
          <input
            type="text"
            placeholder="Tìm kiếm theo tiêu đề, địa chỉ..."
            className="shadow-sm focus:ring-rose-500 focus:border-rose-500 block w-full sm:text-sm border-gray-300 rounded-md"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
            <svg className="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
            </svg>
          </div>
        </div>
        <div className="relative w-full sm:w-64">
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="shadow-sm focus:ring-rose-500 focus:border-rose-500 block w-full sm:text-sm border-gray-300 rounded-md"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang hiển thị</option>
            <option value="PENDING">Đang duyệt</option>
            <option value="EXPIRED">Hết hạn</option>
            <option value="SOLD">Đã bán</option>
          </select>
        </div>
      </div>

      {/* Bảng danh sách */}
      <div className="flex flex-col">
        <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
            <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Bất động sản
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Thông tin
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Trạng thái
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Lượt xem
                    </th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Hành động
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredProperties.length > 0 ? (
                    filteredProperties.map((property) => (
                      <tr key={property.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10 bg-gray-200 rounded">
                              {property.images && property.images.length > 0 && (
                                <img 
                                  src={property.images[0]} 
                                  alt={property.title}
                                  className="h-10 w-10 object-cover rounded"
                                />
                              )}
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">
                                {property.title}
                              </div>
                              <div className="text-sm text-gray-500">
                                {property.address}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900 font-medium">
                            {formatPrice(property.price)}
                          </div>
                          <div className="text-sm text-gray-500">
                            {property.type} • {property.bedrooms || property.beds || 0} PN • {property.bathrooms || property.baths || 0} PT • {property.sqft || property.area || 0} m²
                          </div>
                          <div className="text-xs text-gray-500">
                            Ngày đăng: {new Date(property.createdAt || property.createdDate).toLocaleDateString('vi-VN')}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(property.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {property.views || 0}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <Link 
                            to={`/property/${property.id}`} 
                            className="text-indigo-600 hover:text-indigo-900 mr-4"
                          >
                            Xem
                          </Link>
                          <button
                            onClick={() => onEditProperty(property)}
                            className="text-green-600 hover:text-green-900 mr-4"
                          >
                            Sửa
                          </button>
                          {property.status !== 'SOLD' && (
                            <button
                              onClick={() => handleMarkAsSold(property)}
                              className="text-blue-600 hover:text-blue-900 mr-4"
                            >
                              Đánh dấu đã bán
                            </button>
                          )}
                          <button
                            onClick={() => handleDeleteClick(property)}
                            className="text-red-600 hover:text-red-900"
                          >
                            Xóa
                          </button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="5" className="px-6 py-4 whitespace-nowrap text-center text-gray-500">
                        {searchTerm || filterStatus !== 'all' 
                          ? 'Không tìm thấy tin đăng nào phù hợp với tìm kiếm của bạn.' 
                          : 'Bạn chưa có tin đăng nào. Hãy tạo tin đăng mới!'}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* Delete confirmation modal */}
      {showDeleteModal && (
        <div className="fixed z-10 inset-0 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div className="fixed inset-0 transition-opacity" aria-hidden="true">
              <div className="absolute inset-0 bg-gray-500 opacity-75"></div>
            </div>
            <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
            <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
              <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                <div className="sm:flex sm:items-start">
                  <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10">
                    <svg className="h-6 w-6 text-red-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                  </div>
                  <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">
                      Xác nhận xóa
                    </h3>
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">
                        Bạn có chắc chắn muốn xóa tin đăng "{propertyToDelete?.title}"? Hành động này không thể hoàn tác.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  onClick={confirmDelete}
                  className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:ml-3 sm:w-auto sm:text-sm"
                >
                  Xóa
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowDeleteModal(false);
                    setPropertyToDelete(null);
                  }}
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
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

export default PropertyListingTable; 