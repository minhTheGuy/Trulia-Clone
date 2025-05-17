import { useState, useEffect } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { saveSearchToServer } from '../redux/slices/savedSearchesSlice'
import { toast } from 'react-toastify'

const FilterBar = ({ filters, setFilters }) => {
  const dispatch = useDispatch()
  const { user } = useSelector(state => state.auth)
  const [isFiltersOpen, setIsFiltersOpen] = useState(false)
  const [minPrice, setMinPrice] = useState(filters.priceRange[0])
  const [maxPrice, setMaxPrice] = useState(filters.priceRange[1])
  const [minSqft, setMinSqft] = useState(filters.sqftRange ? filters.sqftRange[0] : 0)
  const [maxSqft, setMaxSqft] = useState(filters.sqftRange ? filters.sqftRange[1] : 1000)
  const [saveSearchModalOpen, setSaveSearchModalOpen] = useState(false)
  const [searchName, setSearchName] = useState('')
  const [isSaving, setIsSaving] = useState(false)

  // Constants - Chuyển sang tiền Việt
  const MAX_PRICE = 100000000000 // 100 tỷ VND
  const STEP = 1000000000 // 1 tỷ VND cho mỗi bước

  // Property type options
  const propertyTypes = [
    { id: 'all', name: 'Tất cả' },
    { id: 'house', name: 'Nhà riêng' },
    { id: 'apartment', name: 'Chung cư' },
    { id: 'condo', name: 'Căn hộ' },
    { id: 'townhouse', name: 'Nhà phố' },
    { id: 'villa', name: 'Biệt thự' },
    { id: 'land', name: 'Đất' },
    { id: 'commercial', name: 'Căn hộ kinh doanh' },
    { id: 'office', name: 'Văn phòng' },
    { id: 'warehouse', name: 'Kho hàng' },
    { id: 'industrial', name: 'Nhà máy' },
    { id: 'hotel', name: 'Khách sạn' },
    { id: 'other', name: 'Khác' }
  ]
  
  // Bedroom options
  const bedroomOptions = [
    { value: 'any', label: 'Tùy chọn' },
    { value: '1', label: '1+' },
    { value: '2', label: '2+' },
    { value: '3', label: '3+' },
    { value: '4', label: '4+' },
    { value: '5', label: '5+' }
  ]
  
  // Bathroom options
  const bathroomOptions = [
    { value: 'any', label: 'Tùy chọn' },
    { value: '1', label: '1+' },
    { value: '1.5', label: '1.5+' },
    { value: '2', label: '2+' },
    { value: '3', label: '3+' },
    { value: '4', label: '4+' }
  ]
  
  // Home type options
  const homeTypeOptions = ['Nhà mặt phố', 'Biệt thự', 'Nhà trong ngõ', 'Căn hộ cao cấp', 'Studio']
  
  // Features options
  const featureOptions = ['Tầng hầm', 'Bể bơi', 'Thang máy', 'Sân vườn', 'Ban công']

  // Sync with parent component when filters change
  useEffect(() => {
    setMinPrice(filters.priceRange[0])
    setMaxPrice(filters.priceRange[1])
    
    if (filters.sqftRange) {
      setMinSqft(filters.sqftRange[0])
      setMaxSqft(filters.sqftRange[1])
    }
    
    // Log homeTypes and features values when they change
    console.log('FilterBar: Filters received from parent:', {
      homeTypes: filters.homeTypes,
      features: filters.features
    });
    
  }, [filters.priceRange, filters.sqftRange, filters.homeTypes, filters.features])

  // Handler functions
  const handleTypeChange = (type) => {
    setFilters({
      ...filters,
      type
    })
  }

  const handleBedroomChange = (e) => {
    setFilters({
      ...filters,
      bedrooms: e.target.value
    })
  }

  const handleBathroomChange = (e) => {
    setFilters({
      ...filters,
      bathrooms: e.target.value
    })
  }

  // Handle price range changes
  const handleMinPriceChange = (value) => {
    const newMin = Math.min(parseInt(value), maxPrice)
    setMinPrice(newMin)
    setFilters({
      ...filters,
      priceRange: [newMin, maxPrice]
    })
  }

  const handleMaxPriceChange = (value) => {
    const newMax = Math.max(parseInt(value), minPrice)
    setMaxPrice(newMax)
    setFilters({
      ...filters,
      priceRange: [minPrice, newMax]
    })
  }
  
  // Handle square footage range changes
  const handleMinSqftChange = (value) => {
    const newMin = Math.min(parseInt(value), maxSqft)
    setMinSqft(newMin)
    setFilters({
      ...filters,
      sqftRange: [newMin, maxSqft]
    })
  }
  
  const handleMaxSqftChange = (value) => {
    const newMax = Math.max(parseInt(value), minSqft)
    setMaxSqft(newMax)
    setFilters({
      ...filters,
      sqftRange: [minSqft, newMax]
    })
  }
  
  // Handle home type changes
  const handleHomeTypeChange = (homeType) => {
    const currentHomeTypes = filters.homeTypes || []
    let updatedHomeTypes
    
    if (currentHomeTypes.includes(homeType)) {
      // Remove home type if already selected
      updatedHomeTypes = currentHomeTypes.filter(type => type !== homeType)
    } else {
      // Add home type if not selected
      updatedHomeTypes = [...currentHomeTypes, homeType]
    }
    
    setFilters({
      ...filters,
      homeTypes: updatedHomeTypes
    })
  }
  
  // Handle features changes
  const handleFeatureChange = (feature) => {
    const currentFeatures = filters.features || []
    let updatedFeatures
    
    if (currentFeatures.includes(feature)) {
      // Remove feature if already selected
      updatedFeatures = currentFeatures.filter(f => f !== feature)
    } else {
      // Add feature if not selected
      updatedFeatures = [...currentFeatures, feature]
    }
    
    setFilters({
      ...filters,
      features: updatedFeatures
    })
  }
  
  // Reset all filters
  const handleResetFilters = () => {
    setFilters({
      type: 'all',
      bedrooms: 'any',
      bathrooms: 'any',
      priceRange: [0, MAX_PRICE],
      sqftRange: [0, 1000],
      homeTypes: [],
      features: []
    })
    
    setMinPrice(0)
    setMaxPrice(MAX_PRICE)
    setMinSqft(0)
    setMaxSqft(1000)
  }

  // Format price to VND
  const formatPrice = (price) => {
    if (!price) return '';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(price);
  };

  // Handle price input change with formatting
  const handlePriceInputChange = (value, setter) => {
    // Remove all non-digit characters
    const rawValue = value.replace(/[^\d]/g, '');
    // Convert to number
    const numValue = Number(rawValue);
    // Update state if valid
    if (!isNaN(numValue)) {
      setter(numValue);
    }
  };

  // Save search handler
  const handleSaveSearch = async () => {
    if (!user) {
      toast.error("Vui lòng đăng nhập để lưu tìm kiếm");
      return;
    }

    if (!searchName.trim()) {
      toast.error("Vui lòng nhập tên cho tìm kiếm của bạn");
      return;
    }

    setIsSaving(true);
    
    try {
      // Prepare search data
      const searchData = {
        name: searchName,
        criteria: {
          ...filters,
          createdAt: new Date().toISOString()
        }
      };
      
      // Dispatch the action to save search
      await dispatch(saveSearchToServer({
        userId: user.id,
        searchData
      })).unwrap();
      
      toast.success("Đã lưu tìm kiếm thành công");
      setSaveSearchModalOpen(false);
      setSearchName('');
    } catch (error) {
      toast.error(typeof error === 'string' ? error : "Không thể lưu tìm kiếm. Vui lòng thử lại sau.");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md mb-8">
      <div className="p-4 md:p-6">
        {/* Header with filters toggle */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-900 mb-2 md:mb-0">Bộ lọc tìm kiếm</h2>
          <div className="flex items-center flex-wrap gap-2">
            <button
              className="text-rose-600 font-medium hover:text-rose-700 flex items-center mr-4"
              onClick={() => setIsFiltersOpen(!isFiltersOpen)}
            >
              {isFiltersOpen ? 'Ẩn bộ lọc' : 'Hiển thị thêm bộ lọc'}
              <svg
                className={`ml-1 h-5 w-5 transform ${isFiltersOpen ? 'rotate-180' : ''}`}
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            
            {user && (
              <button
                className="text-blue-600 font-medium hover:text-blue-700 flex items-center mr-4"
                onClick={() => setSaveSearchModalOpen(true)}
              >
                Lưu tìm kiếm
                <svg 
                  className="ml-1 h-5 w-5" 
                  xmlns="http://www.w3.org/2000/svg" 
                  fill="none" 
                  viewBox="0 0 24 24" 
                  stroke="currentColor"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                </svg>
              </button>
            )}
            
            <button
              className="text-gray-500 font-medium hover:text-gray-700 flex items-center"
              onClick={handleResetFilters}
            >
              Đặt lại bộ lọc
              <svg
                className="ml-1 h-5 w-5"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </button>
          </div>
        </div>

        {/* Always visible filters */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Property type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Loại bất động sản</label>
            <div className="flex flex-wrap gap-2">
              {propertyTypes.map((type) => (
                <button
                  key={type.id}
                  className={`px-3 py-1 rounded-full text-sm ${
                    filters.type === type.id
                      ? 'bg-rose-100 text-rose-700 border border-rose-300'
                      : 'bg-gray-100 text-gray-700 border border-gray-300 hover:bg-gray-200'
                  }`}
                  onClick={() => handleTypeChange(type.id)}
                >
                  {type.name}
                </button>
              ))}
            </div>
          </div>

          {/* Bedrooms */}
          <div>
            <label htmlFor="bedrooms" className="block text-sm font-medium text-gray-700 mb-1">
              Số phòng ngủ
            </label>
            <select
              id="bedrooms"
              name="bedrooms"
              className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm rounded-md"
              value={filters.bedrooms}
              onChange={handleBedroomChange}
            >
              {bedroomOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          {/* Bathrooms */}
          <div>
            <label htmlFor="bathrooms" className="block text-sm font-medium text-gray-700 mb-1">
              Số phòng tắm
            </label>
            <select
              id="bathrooms"
              name="bathrooms"
              className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm rounded-md"
              value={filters.bathrooms}
              onChange={handleBathroomChange}
            >
              {bathroomOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          {/* Price Range - Simplified with separate min/max sliders */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Khoảng giá</label>
            <div className="flex flex-col space-y-4">
              {/* Display current range */}
              <div className="flex justify-between text-sm text-gray-600">
                <span>{formatPrice(minPrice)}</span>
                <span>{formatPrice(maxPrice)}</span>
              </div>
              
              {/* Min Price */}
              <div>
                <label htmlFor="min-price-range" className="sr-only">Giá tối thiểu</label>
                <input
                  type="range"
                  id="min-price-range"
                  min="0"
                  max={MAX_PRICE}
                  step={STEP}
                  value={minPrice}
                  onChange={(e) => handleMinPriceChange(e.target.value)}
                  className="w-full h-2 rounded-lg appearance-none cursor-pointer bg-gray-200"
                />
              </div>
              
              {/* Max Price */}
              <div>
                <label htmlFor="max-price-range" className="sr-only">Giá tối đa</label>
                <input
                  type="range"
                  id="max-price-range"
                  min="0"
                  max={MAX_PRICE}
                  step={STEP}
                  value={maxPrice}
                  onChange={(e) => handleMaxPriceChange(e.target.value)}
                  className="w-full h-2 rounded-lg appearance-none cursor-pointer bg-gray-200"
                />
              </div>
              
              {/* Input fields for direct entry */}
              <div className="flex justify-between space-x-4 mt-2">
                <div className="w-1/2">
                  <label htmlFor="min-price-input" className="block text-xs text-gray-500">Tối thiểu</label>
                  <input
                    type="text"
                    id="min-price-input"
                    value={formatPrice(minPrice)}
                    onChange={(e) => handlePriceInputChange(e.target.value, setMinPrice)}
                    className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                  />
                </div>
                <div className="w-1/2">
                  <label htmlFor="max-price-input" className="block text-xs text-gray-500">Tối đa</label>
                  <input
                    type="text"
                    id="max-price-input"
                    value={formatPrice(maxPrice)}
                    onChange={(e) => handlePriceInputChange(e.target.value, setMaxPrice)}
                    className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Additional filters - shown when expanded */}
        {isFiltersOpen && (
          <div className="mt-6 pt-6 border-t border-gray-200 grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Home type */}
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-3">Loại hình bất động sản</h3>
              <div className="space-y-2">
                {homeTypeOptions.map((option) => (
                  <div key={option} className="flex items-center">
                    <input
                      id={`home-type-${option.toLowerCase().replace(/\s+/g, '-')}`}
                      name="home-type"
                      type="checkbox"
                      checked={(filters.homeTypes || []).includes(option)}
                      onChange={() => handleHomeTypeChange(option)}
                      className="h-4 w-4 text-rose-600 focus:ring-rose-500 border-gray-300 rounded"
                    />
                    <label
                      htmlFor={`home-type-${option.toLowerCase().replace(/\s+/g, '-')}`}
                      className="ml-3 text-sm text-gray-700"
                    >
                      {option}
                    </label>
                  </div>
                ))}
              </div>
            </div>

            {/* Features */}
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-3">Tiện ích</h3>
              <div className="space-y-2">
                {featureOptions.map((option) => (
                  <div key={option} className="flex items-center">
                    <input
                      id={`feature-${option.toLowerCase().replace(/\s+/g, '-')}`}
                      name="feature"
                      type="checkbox"
                      checked={(filters.features || []).includes(option)}
                      onChange={() => handleFeatureChange(option)}
                      className="h-4 w-4 text-rose-600 focus:ring-rose-500 border-gray-300 rounded"
                    />
                    <label
                      htmlFor={`feature-${option.toLowerCase().replace(/\s+/g, '-')}`}
                      className="ml-3 text-sm text-gray-700"
                    >
                      {option}
                    </label>
                  </div>
                ))}
              </div>
            </div>

            {/* Square footage */}
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-3">Diện tích (m²)</h3>
              <div className="space-y-4">
                <div className="flex justify-between text-sm text-gray-600">
                  <span>{minSqft} m²</span>
                  <span>{maxSqft} m²</span>
                </div>
                <div>
                  <label htmlFor="min-sqft-range" className="sr-only">Diện tích tối thiểu</label>
                  <input
                    type="range"
                    id="min-sqft-range"
                    min="0"
                    max="1000"
                    step="10"
                    value={minSqft}
                    onChange={(e) => handleMinSqftChange(e.target.value)}
                    className="w-full h-2 rounded-lg appearance-none cursor-pointer bg-gray-200"
                  />
                </div>
                <div>
                  <label htmlFor="max-sqft-range" className="sr-only">Diện tích tối đa</label>
                  <input
                    type="range"
                    id="max-sqft-range"
                    min="0"
                    max="1000"
                    step="10"
                    value={maxSqft}
                    onChange={(e) => handleMaxSqftChange(e.target.value)}
                    className="w-full h-2 rounded-lg appearance-none cursor-pointer bg-gray-200"
                  />
                </div>
                <div className="flex justify-between space-x-4">
                  <div className="w-1/2">
                    <label htmlFor="min-sqft-input" className="block text-xs text-gray-500">Tối thiểu</label>
                    <input
                      type="number"
                      id="min-sqft-input"
                      min="0"
                      max={maxSqft}
                      value={minSqft}
                      onChange={(e) => handleMinSqftChange(e.target.value)}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                    />
                  </div>
                  <div className="w-1/2">
                    <label htmlFor="max-sqft-input" className="block text-xs text-gray-500">Tối đa</label>
                    <input
                      type="number"
                      id="max-sqft-input"
                      min={minSqft}
                      max="1000"
                      value={maxSqft}
                      onChange={(e) => handleMaxSqftChange(e.target.value)}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {/* Apply filters button - visible when expanded */}
        {isFiltersOpen && (
          <div className="flex justify-end mt-4">
            <button
              type="button"
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
              onClick={() => setIsFiltersOpen(false)}
            >
              Áp dụng bộ lọc
            </button>
          </div>
        )}

        {/* Save Search Modal */}
        {saveSearchModalOpen && (
          <div className="fixed inset-0 overflow-y-auto z-50 flex items-center justify-center">
            <div className="fixed inset-0 bg-black opacity-50"></div>
            <div className="relative bg-white rounded-lg max-w-md w-full p-6 z-10">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-medium text-gray-900">Lưu tìm kiếm</h3>
                <button 
                  className="text-gray-400 hover:text-gray-500" 
                  onClick={() => setSaveSearchModalOpen(false)}
                >
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              
              <div className="mb-4">
                <label htmlFor="search-name" className="block text-sm font-medium text-gray-700 mb-1">
                  Đặt tên cho tìm kiếm này
                </label>
                <input
                  type="text"
                  id="search-name"
                  value={searchName}
                  onChange={(e) => setSearchName(e.target.value)}
                  placeholder="Ví dụ: Nhà riêng ở Hà Nội"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500"
                />
              </div>
              
              <div className="flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setSaveSearchModalOpen(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={handleSaveSearch}
                  disabled={isSaving || !searchName.trim()}
                  className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500 disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  {isSaving ? 'Đang lưu...' : 'Lưu tìm kiếm'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default FilterBar 