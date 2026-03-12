import { useState } from 'react';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

const ScheduleTour = ({ propertyId, sellerInfo, onSubmit, isSubmitting }) => {
  const [tourDate, setTourDate] = useState(new Date());
  const [tourTime, setTourTime] = useState('10:00');
  const [tourType, setTourType] = useState('IN_PERSON');
  const [contactPhone, setContactPhone] = useState('');
  const [message, setMessage] = useState('');
  
  // Set minimum date to tomorrow
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  
  // Available tour times
  const tourTimes = [
    '09:00', '09:30', '10:00', '10:30', '11:00', '11:30', 
    '13:00', '13:30', '14:00', '14:30', '15:00', '15:30', 
    '16:00', '16:30', '17:00'
  ];
  
  const handleSubmit = (e) => {
    e.preventDefault();
    
    const tourData = {
      propertyId,
      tourDate,
      tourTime,
      tourType,
      contactPhone,
      message
      // userId will be added by the PropertyDetails component
    };
    
    onSubmit(tourData);
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-md">
      <h3 className="text-xl font-bold text-gray-900 mb-4">Đặt lịch tham quan</h3>
      
      <div className="flex items-start mb-4">
        <img
          src={sellerInfo?.image || 'https://via.placeholder.com/50'}
          alt={sellerInfo?.name || 'Agent'}
          className="h-12 w-12 rounded-full object-cover mr-3"
        />
        <div>
          <p className="font-medium text-gray-900">{sellerInfo?.name || 'Agent'}</p>
          {sellerInfo?.phone && <p className="text-sm text-gray-500">{sellerInfo.phone}</p>}
          {sellerInfo?.email && <p className="text-sm text-gray-500">{sellerInfo.email}</p>}
        </div>
      </div>
      
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Loại tham quan
          </label>
          <div className="flex space-x-4">
            <label className="inline-flex items-center">
              <input
                type="radio"
                className="form-radio text-rose-600"
                name="tourType"
                value="IN_PERSON"
                checked={tourType === 'IN_PERSON'}
                onChange={e => setTourType(e.target.value)}
              />
              <span className="ml-2">Trực tiếp</span>
            </label>
            <label className="inline-flex items-center">
              <input
                type="radio"
                className="form-radio text-rose-600"
                name="tourType"
                value="VIDEO"
                checked={tourType === 'VIDEO'}
                onChange={e => setTourType(e.target.value)}
              />
              <span className="ml-2">Video</span>
            </label>
          </div>
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Ngày tham quan
          </label>
          <DatePicker
            selected={tourDate}
            onChange={date => setTourDate(date)}
            minDate={tomorrow}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
            dateFormat="dd/MM/yyyy"
          />
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Thời gian
          </label>
          <select
            value={tourTime}
            onChange={e => setTourTime(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
          >
            {tourTimes.map(time => (
              <option key={time} value={time}>
                {time}
              </option>
            ))}
          </select>
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Số điện thoại liên hệ
          </label>
          <input
            type="tel"
            value={contactPhone}
            onChange={e => setContactPhone(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
            placeholder="Nhập số điện thoại của bạn"
            required
          />
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Ghi chú (tùy chọn)
          </label>
          <textarea
            value={message}
            onChange={e => setMessage(e.target.value)}
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
            placeholder="Thông tin thêm bạn muốn chia sẻ..."
          />
        </div>
        
        <button
          type="submit"
          disabled={isSubmitting}
          className={`w-full bg-rose-600 hover:bg-rose-700 text-white font-bold py-2 px-4 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500 ${
            isSubmitting ? 'opacity-70 cursor-not-allowed' : ''
          }`}
        >
          {isSubmitting ? 'Đang xử lý...' : 'Đặt lịch tham quan'}
        </button>
      </form>
    </div>
  );
};

ScheduleTour.propTypes = {
  propertyId: PropTypes.number.isRequired,
  sellerInfo: PropTypes.shape({
    id: PropTypes.number,
    name: PropTypes.string,
    email: PropTypes.string,
    phone: PropTypes.string,
    image: PropTypes.string
  }),
  onSubmit: PropTypes.func.isRequired,
  isSubmitting: PropTypes.bool
};

ScheduleTour.defaultProps = {
  sellerInfo: {},
  isSubmitting: false
};

export default ScheduleTour; 