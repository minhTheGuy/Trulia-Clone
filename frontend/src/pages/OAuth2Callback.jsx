import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { loginWithToken } from '../redux/slices/authSlice'
import authService from '../services/authService'

const OAuth2Callback = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const [errorMsg, setErrorMsg] = useState(null)

  useEffect(() => {
    const token = searchParams.get('token')
    const error = searchParams.get('error')

    if (error) {
      setErrorMsg(decodeURIComponent(error))
      return
    }

    if (!token) {
      setErrorMsg('No authentication token received.')
      return
    }

    try {
      const user = authService.loginWithToken(token)
      dispatch(loginWithToken(user))

      // Redirect based on role, same logic as SignIn
      if (user.roles?.includes('ROLE_ADMIN')) {
        navigate('/admin', { replace: true })
      } else if (user.roles?.includes('ROLE_SELLER')) {
        navigate('/dashboard', { replace: true })
      } else {
        navigate('/', { replace: true })
      }
    } catch (e) {
      setErrorMsg(e.message || 'Authentication failed. Please try again.')
    }
  }, [searchParams, dispatch, navigate])

  if (errorMsg) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 text-center">
            <div className="mb-4 text-red-500">
              <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">Đăng nhập thất bại</h2>
            <p className="text-sm text-gray-600 mb-6">{errorMsg}</p>
            <button
              onClick={() => navigate('/signin', { replace: true })}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-rose-600 hover:bg-rose-700"
            >
              Quay lại trang đăng nhập
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-rose-600 mx-auto mb-4"></div>
        <p className="text-gray-600">Đang xử lý đăng nhập...</p>
      </div>
    </div>
  )
}

export default OAuth2Callback
