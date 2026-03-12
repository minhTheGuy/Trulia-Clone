import { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import axios from 'axios'

const ResetPassword = () => {
  const { token } = useParams()
  const navigate = useNavigate()
  
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [tokenValidated, setTokenValidated] = useState(false)
  const [username, setUsername] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  
  // Verify token when component loads
  useEffect(() => {
    const validateToken = async () => {
      try {
        const response = await axios.get(`/api/auth/password/validate-token?token=${token}`)
        if (response.data) {
          setTokenValidated(true)
          setUsername(response.data.username)
        }
      } catch {
        setErrorMessage('Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.')
      }
    }
    
    if (token) {
      validateToken()
    } else {
      setErrorMessage('Không tìm thấy mã xác thực. Vui lòng yêu cầu liên kết đặt lại mật khẩu mới.')
    }
  }, [token])
  
  const handleSubmit = async (e) => {
    e.preventDefault()
    
    // Validate passwords match
    if (newPassword !== confirmPassword) {
      setErrorMessage('Mật khẩu nhập lại không khớp.')
      return
    }
    
    setLoading(true)
    setErrorMessage('')
    
    try {
      const response = await axios.post('/api/auth/password/reset', {
        token,
        newPassword
      })
      
      setSuccessMessage(response.data.message)
      
      // Redirect to login after 3 seconds
      setTimeout(() => {
        navigate('/signin')
      }, 3000)
      
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || 
        'Có lỗi xảy ra. Vui lòng thử lại sau.'
      )
    } finally {
      setLoading(false)
    }
  }
  
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <Link 
          to="/" 
          className="flex items-center justify-center cursor-pointer"
        >
          <span className="text-3xl font-bold text-rose-600">Trulia</span>
          <span className="ml-1 text-3xl font-bold text-gray-900">Clone</span>
        </Link>
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">Đặt lại mật khẩu</h2>
        {tokenValidated && username && (
          <p className="mt-2 text-center text-sm text-gray-600">
            Đặt mật khẩu mới cho tài khoản <span className="font-medium">{username}</span>
          </p>
        )}
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          {successMessage && (
            <div className="mb-4 rounded-md bg-green-50 p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-green-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-sm font-medium text-green-800">{successMessage}</p>
                  <p className="mt-2 text-sm text-green-700">Bạn sẽ được chuyển đến trang đăng nhập trong giây lát...</p>
                </div>
              </div>
            </div>
          )}

          {errorMessage && (
            <div className="mb-4 rounded-md bg-red-50 p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-sm font-medium text-red-800">{errorMessage}</p>
                  {!tokenValidated && (
                    <div className="mt-2">
                      <Link to="/forgot-password" className="text-sm font-medium text-red-600 hover:text-red-500">
                        Yêu cầu liên kết đặt lại mật khẩu mới
                      </Link>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {tokenValidated && !successMessage && (
            <form className="space-y-6" onSubmit={handleSubmit}>
              <div>
                <label htmlFor="new-password" className="block text-sm font-medium text-gray-700">
                  Mật khẩu mới
                </label>
                <div className="mt-1">
                  <input
                    id="new-password"
                    name="new-password"
                    type="password"
                    autoComplete="new-password"
                    required
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                    placeholder="Nhập mật khẩu mới"
                    minLength="6"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="confirm-password" className="block text-sm font-medium text-gray-700">
                  Xác nhận mật khẩu
                </label>
                <div className="mt-1">
                  <input
                    id="confirm-password"
                    name="confirm-password"
                    type="password"
                    autoComplete="new-password"
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                    placeholder="Nhập lại mật khẩu mới"
                  />
                </div>
              </div>

              <div>
                <button
                  type="submit"
                  disabled={loading}
                  className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white ${loading ? 'bg-rose-400' : 'bg-rose-600 hover:bg-rose-700'} focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500`}
                >
                  {loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu'}
                </button>
              </div>
            </form>
          )}

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">Hoặc</span>
              </div>
            </div>

            <div className="mt-6 flex items-center justify-center">
              <div className="text-sm">
                <Link to="/signin" className="font-medium text-rose-600 hover:text-rose-500">
                  Quay lại đăng nhập
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ResetPassword 