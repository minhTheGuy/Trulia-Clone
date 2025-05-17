import { useState, useCallback, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { searchPropertiesByKeyword } from '../redux/slices/propertySlice'

const Hero = () => {
  const [searchType, setSearchType] = useState('buy')
  const [searchQuery, setSearchQuery] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const timeoutRef = useRef(null)
  const navigate = useNavigate()
  const dispatch = useDispatch()

  // Use useCallback to ensure this function isn't recreated unnecessarily
  const handleSearch = useCallback(async (e) => {
    e.preventDefault()
    
    // Clear any previous error messages and timeout
    setErrorMessage('')
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current)
    }
    
    // Validate input
    if (!searchQuery.trim()) return
    
    // Prevent multiple submissions
    if (isSubmitting) return
    
    // Set submitting state to prevent multiple submissions
    setIsSubmitting(true)
    
    // Set a timeout to cancel the search if it takes too long
    timeoutRef.current = setTimeout(() => {
      setIsSubmitting(false)
      setErrorMessage('Tìm kiếm mất quá nhiều thời gian. Vui lòng thử lại sau.')
    }, 8000) // 8 seconds timeout
    
    try {
      // Use the appropriate search type parameter based on the selected tab
      const searchParams = {
        keyword: searchQuery,
        pageNumber: 0,
        pageSize: 10,
        sortBy: 'price',
        sortOrder: 'asc',
      }
      
      // Add property type filter based on selected search type
      if (searchType === 'buy') {
        searchParams.forSale = true;
      } else if (searchType === 'rent') {
        searchParams.forRent = true;
      } else if (searchType === 'sold') {
        searchParams.status = 'SOLD';
      }
      
      // Dispatch search action
      await dispatch(searchPropertiesByKeyword(searchParams)).unwrap()
      
      // Clear timeout on success
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
        timeoutRef.current = null
      }
      
      // Navigate to homepage with search results only after successful dispatch
      navigate(`/?keyword=${encodeURIComponent(searchQuery)}&type=${searchType}`)
    } catch (error) {
      console.error("Search error:", error)
      
      // Clear timeout
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
        timeoutRef.current = null
      }
      
      setErrorMessage(error.response?.data?.message || 'Có lỗi khi tìm kiếm. Vui lòng thử lại sau.')
      
      // Still navigate to show the error in PropertyList
      navigate(`/?keyword=${encodeURIComponent(searchQuery)}&type=${searchType}&error=true`)
    } finally {
      setIsSubmitting(false)
    }
  }, [searchQuery, searchType, isSubmitting, dispatch, navigate])
  
  // Clean up timeout on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
    }
  }, [])

  return (
    <div className="relative bg-gray-900">
      {/* Background image */}
      <div className="absolute inset-0 overflow-hidden">
        <img
          src="https://images.unsplash.com/photo-1560518883-ce09059eeffa?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1973&q=80"
          alt="Những ngôi nhà đẹp"
          className="w-full h-full object-cover object-center"
        />
        <div className="absolute inset-0 bg-black bg-opacity-60"></div>
      </div>

      <div className="relative max-w-7xl mx-auto px-4 py-24 sm:px-6 lg:px-8 flex flex-col items-center">
        <h1 className="text-4xl font-extrabold tracking-tight text-white sm:text-5xl lg:text-6xl text-center">
          Tìm Ngôi Nhà Mơ Ước Của Bạn
        </h1>
        <p className="mt-6 text-xl text-white max-w-3xl text-center">
          Tìm kiếm hàng triệu ngôi nhà và căn hộ để bán và cho thuê trên khắp Việt Nam
        </p>

        {/* Search box */}
        <div className="mt-10 w-full max-w-3xl">
          <div className="bg-white rounded-lg shadow-xl overflow-hidden">
            {/* Search type tabs */}
            <div className="flex border-b">
              <button
                type="button"
                disabled={isSubmitting}
                className={`flex-1 py-3 text-center font-medium ${
                  searchType === 'buy' ? 'text-rose-600 border-b-2 border-rose-600' : 'text-gray-500 hover:text-gray-700'
                } ${isSubmitting ? 'opacity-50 cursor-not-allowed' : ''}`}
                onClick={() => setSearchType('buy')}
              >
                Mua
              </button>
              <button
                type="button"
                disabled={isSubmitting}
                className={`flex-1 py-3 text-center font-medium ${
                  searchType === 'rent' ? 'text-rose-600 border-b-2 border-rose-600' : 'text-gray-500 hover:text-gray-700'
                } ${isSubmitting ? 'opacity-50 cursor-not-allowed' : ''}`}
                onClick={() => setSearchType('rent')}
              >
                Thuê
              </button>
              <button
                type="button"
                disabled={isSubmitting}
                className={`flex-1 py-3 text-center font-medium ${
                  searchType === 'sold' ? 'text-rose-600 border-b-2 border-rose-600' : 'text-gray-500 hover:text-gray-700'
                } ${isSubmitting ? 'opacity-50 cursor-not-allowed' : ''}`}
                onClick={() => setSearchType('sold')}
              >
                Đã bán
              </button>
            </div>

            {/* Search input */}
            <form onSubmit={handleSearch} className="flex flex-col px-4 py-4">
              <div className="flex items-center">
                <div className="flex-grow">
                  <label htmlFor="search" className="sr-only">
                    Tìm kiếm
                  </label>
                  <input
                    type="text"
                    id="search"
                    placeholder="Địa chỉ, Thành phố, hoặc Mã bưu chính"
                    className="block w-full text-gray-900 border-0 focus:ring-0 placeholder-gray-500 text-lg"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    disabled={isSubmitting}
                  />
                </div>
                <div className="ml-4">
                  <button
                    type="submit"
                    className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500 disabled:bg-rose-400 disabled:cursor-not-allowed"
                    disabled={isSubmitting || !searchQuery.trim()}
                  >
                    {isSubmitting ? (
                      <>
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Đang tìm...
                      </>
                    ) : 'Tìm kiếm'}
                  </button>
                </div>
              </div>
              
              {/* Error message */}
              {errorMessage && (
                <div className="mt-2 text-sm text-red-600 bg-red-50 p-2 rounded">
                  <p>{errorMessage}</p>
                  <button 
                    type="button"
                    className="text-rose-700 mt-1 underline focus:outline-none"
                    onClick={() => setErrorMessage('')}
                  >
                    Thử lại
                  </button>
                </div>
              )}
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Hero 