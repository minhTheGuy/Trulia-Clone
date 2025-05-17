import { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import propertyService from '../../services/propertyService';
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import vi from 'date-fns/locale/vi';

const ScheduledToursList = () => {
  const [tours, setTours] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const { user } = useSelector((state) => state.auth);
  const [properties, setProperties] = useState({});

  // Fetch tours on component mount
  useEffect(() => {
    fetchTours();
  }, [filter]);

  // Fetch associated property details for each tour
  useEffect(() => {
    if (tours.length > 0) {
      const propertyIds = [...new Set(tours.map(tour => tour.propertyId))];
      fetchPropertyDetails(propertyIds);
    }
  }, [tours]);

  const fetchTours = async () => {
    if (!user?.id) return;
    
    setLoading(true);
    try {
      let tourData;
      
      if (filter === 'ALL') {
        tourData = await propertyService.getSellerTours(user.id);
      } else {
        tourData = await propertyService.getSellerToursByStatus(user.id, filter);
      }
      
      setTours(tourData);
    } catch (error) {
      console.error('Error fetching tours:', error);
      toast.error('Không thể tải lịch tham quan');
    } finally {
      setLoading(false);
    }
  };

  const fetchPropertyDetails = async (propertyIds) => {
    try {
      const propertyDetails = {};
      
      // This can be optimized with a bulk property fetch if the API supports it
      for (const propertyId of propertyIds) {
        try {
          const property = await propertyService.getPropertyById(propertyId);
          propertyDetails[propertyId] = property;
        } catch (err) {
          console.error(`Error fetching property ${propertyId}:`, err);
          propertyDetails[propertyId] = { title: 'Không tìm thấy thông tin', address: 'Không có địa chỉ' };
        }
      }
      
      setProperties(propertyDetails);
    } catch (error) {
      console.error('Error fetching property details:', error);
    }
  };

  const handleUpdateStatus = async (tourId, newStatus) => {
    try {
      await propertyService.updateTourStatus(tourId, newStatus);
      toast.success('Cập nhật trạng thái thành công');
      
      // Update the tour status in the local state
      setTours(prevTours => 
        prevTours.map(tour => 
          tour.id === tourId ? { ...tour, status: newStatus } : tour
        )
      );
    } catch (error) {
      console.error('Error updating tour status:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const handleCancelTour = async (tourId) => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy lịch tham quan này không?')) {
      return;
    }
    
    try {
      await propertyService.cancelTour(tourId);
      toast.success('Hủy lịch tham quan thành công');
      
      // Update the tour status in the local state
      setTours(prevTours => 
        prevTours.map(tour => 
          tour.id === tourId ? { ...tour, status: 'CANCELLED' } : tour
        )
      );
    } catch (error) {
      console.error('Error cancelling tour:', error);
      toast.error('Không thể hủy lịch tham quan');
    }
  };

  // Returns a appropriate color for tour status badge
  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'CONFIRMED': return 'bg-green-100 text-green-800';
      case 'COMPLETED': return 'bg-blue-100 text-blue-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // Format status for display
  const formatStatus = (status) => {
    switch (status) {
      case 'PENDING': return 'Đang chờ';
      case 'CONFIRMED': return 'Đã xác nhận';
      case 'COMPLETED': return 'Đã hoàn thành';
      case 'CANCELLED': return 'Đã hủy';
      default: return status;
    }
  };

  // Format tour type for display
  const formatTourType = (type) => {
    return type === 'IN_PERSON' ? 'Trực tiếp' : 'Video';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-rose-500"></div>
      </div>
    );
  }

  return (
    <div className="bg-white shadow overflow-hidden sm:rounded-lg">
      <div className="px-4 py-5 sm:px-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900">Lịch tham quan bất động sản</h3>
        <p className="mt-1 max-w-2xl text-sm text-gray-500">
          Danh sách lịch tham quan bất động sản của bạn.
        </p>
      </div>

      {/* Filter options */}
      <div className="px-4 sm:px-6 py-2 bg-gray-50 border-t border-b border-gray-200">
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => setFilter('ALL')}
            className={`px-3 py-1 rounded-full text-sm font-medium ${
              filter === 'ALL' ? 'bg-rose-100 text-rose-800' : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
            }`}
          >
            Tất cả
          </button>
          <button
            onClick={() => setFilter('PENDING')}
            className={`px-3 py-1 rounded-full text-sm font-medium ${
              filter === 'PENDING' ? 'bg-yellow-100 text-yellow-800' : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
            }`}
          >
            Đang chờ
          </button>
          <button
            onClick={() => setFilter('CONFIRMED')}
            className={`px-3 py-1 rounded-full text-sm font-medium ${
              filter === 'CONFIRMED' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
            }`}
          >
            Đã xác nhận
          </button>
          <button
            onClick={() => setFilter('COMPLETED')}
            className={`px-3 py-1 rounded-full text-sm font-medium ${
              filter === 'COMPLETED' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
            }`}
          >
            Đã hoàn thành
          </button>
          <button
            onClick={() => setFilter('CANCELLED')}
            className={`px-3 py-1 rounded-full text-sm font-medium ${
              filter === 'CANCELLED' ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-800 hover:bg-gray-200'
            }`}
          >
            Đã hủy
          </button>
        </div>
      </div>

      {tours.length === 0 ? (
        <div className="text-center py-10">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900">Không có lịch tham quan nào</h3>
          <p className="mt-1 text-sm text-gray-500">
            Hiện tại không có lịch tham quan nào {filter !== 'ALL' ? `với trạng thái ${formatStatus(filter)}` : ''}.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Bất động sản
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Khách hàng
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Thời gian
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Loại
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Trạng thái
                </th>
                <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {tours.map((tour) => (
                <tr key={tour.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="flex-shrink-0 h-10 w-10">
                        <img 
                          className="h-10 w-10 rounded-md object-cover" 
                          src={properties[tour.propertyId]?.images?.[0] || 'https://via.placeholder.com/150'} 
                          alt="" 
                        />
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">
                          {properties[tour.propertyId]?.title || 'Đang tải...'}
                        </div>
                        <div className="text-sm text-gray-500">
                          {properties[tour.propertyId]?.address || 'Đang tải...'}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{tour.userName || 'Khách hàng'}</div>
                    <div className="text-sm text-gray-500">{tour.contactPhone}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {tour.tourDate && format(new Date(tour.tourDate), 'dd/MM/yyyy', { locale: vi })}
                    </div>
                    <div className="text-sm text-gray-500">{tour.tourTime}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatTourType(tour.tourType)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(tour.status)}`}>
                      {formatStatus(tour.status)}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    {tour.status === 'PENDING' && (
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => handleUpdateStatus(tour.id, 'CONFIRMED')}
                          className="text-green-600 hover:text-green-900 bg-green-50 px-2 py-1 rounded"
                        >
                          Xác nhận
                        </button>
                        <button
                          onClick={() => handleCancelTour(tour.id)}
                          className="text-red-600 hover:text-red-900 bg-red-50 px-2 py-1 rounded"
                        >
                          Hủy
                        </button>
                      </div>
                    )}
                    {tour.status === 'CONFIRMED' && (
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => handleUpdateStatus(tour.id, 'COMPLETED')}
                          className="text-blue-600 hover:text-blue-900 bg-blue-50 px-2 py-1 rounded"
                        >
                          Hoàn thành
                        </button>
                        <button
                          onClick={() => handleCancelTour(tour.id)}
                          className="text-red-600 hover:text-red-900 bg-red-50 px-2 py-1 rounded"
                        >
                          Hủy
                        </button>
                      </div>
                    )}
                    {(tour.status === 'COMPLETED' || tour.status === 'CANCELLED') && (
                      <span className="text-gray-400">Không có thao tác</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ScheduledToursList; 