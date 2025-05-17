import { useState, useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useNavigate } from 'react-router-dom'
import { register, clearError } from '../redux/slices/authSlice'

const SignUp = () => {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { isLoggedIn, error, loading } = useSelector((state) => state.auth)
  
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [validationError, setValidationError] = useState('')
  const [registrationSuccess, setRegistrationSuccess] = useState(false)

  useEffect(() => {
    // Clear any previous errors when component mounts
    dispatch(clearError())
  }, [dispatch])

  useEffect(() => {
    // Redirect if user is already logged in
    if (isLoggedIn) {
      navigate('/')
    }
  }, [isLoggedIn, navigate])

  const handleSubmit = (e) => {
    e.preventDefault()
    setValidationError('')
    setRegistrationSuccess(false)

    // Kiểm tra mật khẩu khớp
    if (password !== confirmPassword) {
      setValidationError('Mật khẩu nhập lại không khớp')
      return
    }

    // Kiểm tra đồng ý điều khoản
    if (!agreeTerms) {
      setValidationError('Vui lòng đồng ý với điều khoản dịch vụ')
      return
    }

    // Dispatch register action
    dispatch(register({ 
      username, 
      email, 
      password, 
      firstName, 
      lastName, 
      phoneNumber 
    })).unwrap()
      .then(() => {
        // Registration successful - show success message
        setRegistrationSuccess(true)
        // Clear form
        setUsername('')
        setEmail('')
        setPassword('')
        setConfirmPassword('')
        setFirstName('')
        setLastName('')
        setPhoneNumber('')
        setAgreeTerms(false)
      })
      .catch(() => {
        // Error handling is done by the reducer
      })
  }

  // Display either validation error or Redux error
  const displayError = validationError || error

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
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">Đăng ký tài khoản</h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Hoặc{' '}
          <Link 
            to="/signin"
            className="font-medium text-rose-600 hover:text-rose-500 focus:outline-none"
          >
            đăng nhập nếu đã có tài khoản
          </Link>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          {registrationSuccess ? (
            <div className="mb-4 bg-green-50 border-l-4 border-green-500 p-4 text-green-700">
              <h3 className="font-medium">Đăng ký thành công!</h3>
              <p className="mt-2">Vui lòng kiểm tra email của bạn để xác thực tài khoản trước khi đăng nhập.</p>
              <div className="mt-4">
                <button
                  onClick={() => navigate('/signin')}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                >
                  Đi đến trang đăng nhập
                </button>
              </div>
            </div>
          ) : (
            <>
              {displayError && (
                <div className="mb-4 bg-red-50 border-l-4 border-red-500 p-4 text-red-700">
                  <p>{displayError}</p>
                </div>
              )}
              
              <form className="space-y-6" onSubmit={handleSubmit}>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="first-name" className="block text-sm font-medium text-gray-700">
                      Họ
                    </label>
                    <div className="mt-1">
                      <input
                        id="first-name"
                        name="first-name"
                        type="text"
                        autoComplete="given-name"
                        required
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                        placeholder="Nhập họ"
                      />
                    </div>
                  </div>

                  <div>
                    <label htmlFor="last-name" className="block text-sm font-medium text-gray-700">
                      Tên
                    </label>
                    <div className="mt-1">
                      <input
                        id="last-name"
                        name="last-name"
                        type="text"
                        autoComplete="family-name"
                        required
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                        placeholder="Nhập tên"
                      />
                    </div>
                  </div>
                </div>

                <div>
                  <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                    Tên đăng nhập
                  </label>
                  <div className="mt-1">
                    <input
                      id="username"
                      name="username"
                      type="text"
                      autoComplete="username"
                      required
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                      placeholder="Nhập tên đăng nhập"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                    Email
                  </label>
                  <div className="mt-1">
                    <input
                      id="email"
                      name="email"
                      type="email"
                      autoComplete="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                      placeholder="Nhập email của bạn"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="phone-number" className="block text-sm font-medium text-gray-700">
                    Số điện thoại
                  </label>
                  <div className="mt-1">
                    <input
                      id="phone-number"
                      name="phone-number"
                      type="tel"
                      autoComplete="tel"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                      className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                      placeholder="Nhập số điện thoại của bạn"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                    Mật khẩu
                  </label>
                  <div className="mt-1">
                    <input
                      id="password"
                      name="password"
                      type="password"
                      autoComplete="new-password"
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                      placeholder="Nhập mật khẩu mới"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="confirm-password" className="block text-sm font-medium text-gray-700">
                    Nhập lại mật khẩu
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

                <div className="flex items-center">
                  <input
                    id="terms"
                    name="terms"
                    type="checkbox"
                    checked={agreeTerms}
                    onChange={(e) => setAgreeTerms(e.target.checked)}
                    className="h-4 w-4 text-rose-600 focus:ring-rose-500 border-gray-300 rounded"
                  />
                  <label htmlFor="terms" className="ml-2 block text-sm text-gray-900">
                    Tôi đồng ý với <button type="button" className="text-rose-600 hover:text-rose-500">Điều khoản sử dụng</button> và <button type="button" className="text-rose-600 hover:text-rose-500">Chính sách bảo mật</button>
                  </label>
                </div>

                <div>
                  <button
                    type="submit"
                    disabled={loading}
                    className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white ${
                      loading ? 'bg-rose-300' : 'bg-rose-600 hover:bg-rose-700'
                    } focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500`}
                  >
                    {loading ? (
                      <>
                        <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Đang xử lý...
                      </>
                    ) : 'Đăng ký'}
                  </button>
                </div>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default SignUp 