import { useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'

export default function PaymentSuccess() {
  const [searchParams] = useSearchParams()
  const sessionId = searchParams.get('session_id')

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [])

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-lg p-8 text-center">
        <div className="flex items-center justify-center w-16 h-16 rounded-full bg-green-100 mx-auto mb-4">
          <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2">Thanh toán thành công!</h1>
        <p className="text-gray-600 mb-6">
          Giao dịch thuê nhà của bạn đã được xử lý. Chúng tôi sẽ liên hệ xác nhận trong thời gian sớm nhất.
        </p>

        {sessionId && (
          <p className="text-xs text-gray-400 mb-6 font-mono break-all">
            Mã phiên: {sessionId}
          </p>
        )}

        <div className="flex flex-col gap-3">
          <Link
            to="/"
            className="w-full inline-flex justify-center items-center px-6 py-3 bg-rose-600 hover:bg-rose-700 text-white font-semibold rounded-lg transition-colors"
          >
            Về trang chủ
          </Link>
          <Link
            to="/profile"
            className="w-full inline-flex justify-center items-center px-6 py-3 border border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold rounded-lg transition-colors"
          >
            Xem hồ sơ của tôi
          </Link>
        </div>
      </div>
    </div>
  )
}
