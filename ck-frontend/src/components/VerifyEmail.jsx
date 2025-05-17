import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import authService from '../services/authService';

const VerifyEmail = () => {
  const [verificationStatus, setVerificationStatus] = useState('loading');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const verifyEmailToken = async () => {
      // Get token from URL query parameter
      const searchParams = new URLSearchParams(location.search);
      const token = searchParams.get('token');

      if (!token) {
        setVerificationStatus('error');
        setMessage('Không tìm thấy mã xác thực. Vui lòng kiểm tra lại đường dẫn hoặc liên hệ hỗ trợ.');
        return;
      }

      try {
        // Call the verification API
        const response = await authService.verifyEmail(token);
        
        setVerificationStatus('success');
        setMessage(response.message || 'Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.');
      } catch (error) {
        console.error("Verification error:", error);
        setVerificationStatus('error');
        setMessage(
          error.response?.data?.message || 
          'Xác thực email thất bại. Liên kết có thể đã hết hạn hoặc không hợp lệ.'
        );
      }
    };

    verifyEmailToken();
  }, [location]);

  const redirectToLogin = () => {
    navigate('/signin');
  };

  const redirectToHome = () => {
    navigate('/');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            {verificationStatus === 'loading' ? 'Đang xác thực...' :
             verificationStatus === 'success' ? 'Xác thực thành công!' :
             'Xác thực thất bại'}
          </h2>
          <div className="mt-2 text-center text-sm text-gray-600">
            {verificationStatus === 'loading' && (
              <div className="flex justify-center mt-5">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
              </div>
            )}
            
            {(verificationStatus === 'success' || verificationStatus === 'error') && (
              <p className="font-medium text-indigo-600 mt-2">
                {message}
              </p>
            )}
          </div>
        </div>
        
        <div className="mt-5 flex justify-center space-x-4">
          {verificationStatus === 'success' && (
            <button
              onClick={redirectToLogin}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
              Đăng nhập ngay
            </button>
          )}
          
          <button
            onClick={redirectToHome}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            Về trang chủ
          </button>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmail; 