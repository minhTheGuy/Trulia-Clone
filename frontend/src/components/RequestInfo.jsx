import { useState } from 'react';
import PropTypes from 'prop-types';

const RequestInfo = ({ propertyId, propertyTitle, onSubmit, isSubmitting }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [message, setMessage] = useState('');
  const [preferredContact, setPreferredContact] = useState('EMAIL');

  const handleSubmit = (e) => {
    e.preventDefault();
    
    const requestData = {
      propertyId,
      name,
      email,
      phone,
      message,
      preferredContact,
      propertyTitle
    };
    
    onSubmit(requestData);
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-md">
      <h3 className="text-xl font-bold text-gray-900 mb-4">Yêu cầu thông tin</h3>
      
      <p className="text-gray-600 mb-4">
        Điền thông tin của bạn để nhận thêm chi tiết về bất động sản này
      </p>
      
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Họ tên
          </label>
          <input
            type="text"
            value={name}
            onChange={e => setName(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
            placeholder="Nhập họ tên của bạn"
            required
          />
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Email
          </label>
          <input
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
            placeholder="Nhập email của bạn"
            required
          />
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Số điện thoại
          </label>
          <input
            type="tel"
            value={phone}
            onChange={e => setPhone(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
            placeholder="Nhập số điện thoại của bạn"
            required
          />
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Phương thức liên hệ ưa thích
          </label>
          <div className="flex space-x-4">
            <label className="inline-flex items-center">
              <input
                type="radio"
                className="form-radio text-rose-600"
                name="preferredContact"
                value="EMAIL"
                checked={preferredContact === 'EMAIL'}
                onChange={e => setPreferredContact(e.target.value)}
              />
              <span className="ml-2">Email</span>
            </label>
            <label className="inline-flex items-center">
              <input
                type="radio"
                className="form-radio text-rose-600"
                name="preferredContact"
                value="PHONE"
                checked={preferredContact === 'PHONE'}
                onChange={e => setPreferredContact(e.target.value)}
              />
              <span className="ml-2">Điện thoại</span>
            </label>
            <label className="inline-flex items-center">
              <input
                type="radio"
                className="form-radio text-rose-600"
                name="preferredContact"
                value="BOTH"
                checked={preferredContact === 'BOTH'}
                onChange={e => setPreferredContact(e.target.value)}
              />
              <span className="ml-2">Cả hai</span>
            </label>
          </div>
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Tin nhắn (tùy chọn)
          </label>
          <textarea
            value={message}
            onChange={e => setMessage(e.target.value)}
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
            placeholder="Câu hỏi hoặc thông tin cụ thể bạn quan tâm..."
          />
        </div>
        
        <button
          type="submit"
          disabled={isSubmitting}
          className={`w-full bg-rose-600 hover:bg-rose-700 text-white font-bold py-2 px-4 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500 ${
            isSubmitting ? 'opacity-70 cursor-not-allowed' : ''
          }`}
        >
          {isSubmitting ? 'Đang xử lý...' : 'Gửi yêu cầu'}
        </button>
      </form>
    </div>
  );
};

RequestInfo.propTypes = {
  propertyId: PropTypes.number.isRequired,
  propertyTitle: PropTypes.string,
  onSubmit: PropTypes.func.isRequired,
  isSubmitting: PropTypes.bool
};

RequestInfo.defaultProps = {
  propertyTitle: '',
  isSubmitting: false
};

export default RequestInfo; 