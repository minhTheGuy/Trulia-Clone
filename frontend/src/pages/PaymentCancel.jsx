import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export default function PaymentCancel() {
  const navigate = useNavigate()

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [])

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-lg p-8 text-center">
        <div className="flex items-center justify-center w-16 h-16 rounded-full bg-yellow-100 mx-auto mb-4">
          <svg className="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M12 3a9 9 0 100 18A9 9 0 0012 3z" />
          </svg>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2">Thanh toán bị hủy</h1>
        <p className="text-gray-600 mb-6">
          Bạn đã hủy thanh toán. Giao dịch chưa được hoàn tất. Bạn có thể thử lại bất kỳ lúc nào.
        </p>

        <div className="flex flex-col gap-3">
          <button
            onClick={() => navigate(-1)}
            className="w-full inline-flex justify-center items-center px-6 py-3 bg-rose-600 hover:bg-rose-700 text-white font-semibold rounded-lg transition-colors"
          >
            Quay lại
          </button>
          <Link
            to="/"
            className="w-full inline-flex justify-center items-center px-6 py-3 border border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold rounded-lg transition-colors"
          >
            Về trang chủ
          </Link>
        </div>
      </div>
    </div>
  )
}
