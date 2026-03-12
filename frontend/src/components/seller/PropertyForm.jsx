import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createProperty, updateProperty, clearPropertyError, clearPropertySuccess } from '../../redux/slices/propertySlice';
import PropertyMapForm from './PropertyMapForm';

const PropertyForm = ({ propertyData }) => {
  const dispatch = useDispatch();
  const { loading, error, success } = useSelector(state => state.properties);
  const [formSubmitted, setFormSubmitted] = useState(false);
  const [uploadedImages, setUploadedImages] = useState([]);
  const [previewImages, setPreviewImages] = useState([]);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const { user } = useSelector(state => state.auth);
  
  // Form state
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: '',
    address: '',
    city: '',
    district: '',
    zipCode: '',
    type: '',
    bedrooms: '',
    bathrooms: '',
    sqft: '',
    yearBuilt: '',
    forSale: true,
    forRent: false,
    features: [],
    categoryId: 1, // Default category ID
    userId: user?.id || 0, // Default user ID
    status: 'PENDING', // Default status
    // Location fields
    latitude: 10.762622, // Default: Ho Chi Minh City center
    longitude: 106.660172,
    // Address details
    street: '',
    state: '',
    // Neighborhood
    neighborhoodName: '',
    neighborhoodDescription: '',
    neighborhoodAmenities: [],
    walkScore: 0,
    transitScore: 0,
    bikeScore: 0,
    // Agent info
    agentName: user?.name || '',
    agentPhone: user?.phone || '',
    agentEmail: user?.email || '',
    agentImage: '',
    agentCompany: '',
    lotSize: '',
    daysOnMarket: 0,
  });

  // Các loại bất động sản
  const propertyTypes = [
    { value: '', label: 'Chọn loại bất động sản' },
    { value: 'house', label: 'Nhà riêng' },
    { value: 'apartment', label: 'Chung cư' },
    { value: 'villa', label: 'Biệt thự' },
    { value: 'condo', label: 'Căn hộ' },
    { value: 'townhouse', label: 'Nhà phố' },
    { value: 'land', label: 'Đất nền' },
    { value: 'office', label: 'Văn phòng' },
    { value: 'commercial', label: 'Mặt bằng kinh doanh' }
  ];

  // Các trạng thái bất động sản
  const propertyStatuses = [
    { value: 'PENDING', label: 'Đang duyệt' },
    { value: 'ACTIVE', label: 'Đang hiển thị' },
    { value: 'EXPIRED', label: 'Hết hạn' },
    { value: 'SOLD', label: 'Đã bán' }
  ];

  // Các danh mục bất động sản
  const propertyCategories = [
    { id: 1, name: 'Nhà ở' },
    { id: 2, name: 'Căn hộ' },
    { id: 3, name: 'Đất' },
    { id: 4, name: 'Văn phòng' },
    { id: 5, name: 'Nhà cho thuê' }
  ];

  // Tính năng bất động sản
  const availableFeatures = [
    { id: 'pool', name: 'Hồ bơi' },
    { id: 'garden', name: 'Sân vườn' },
    { id: 'garage', name: 'Gara ô tô' },
    { id: 'security', name: 'An ninh 24/7' },
    { id: 'gym', name: 'Phòng tập gym' },
    { id: 'airCon', name: 'Điều hòa' },
    { id: 'furnished', name: 'Nội thất đầy đủ' },
    { id: 'elevator', name: 'Thang máy' },
    { id: 'terrace', name: 'Ban công/Sân thượng' },
    { id: 'parking', name: 'Bãi đỗ xe' }
  ];

  // Reset messages and redux state when component mounts or propertyData changes
  useEffect(() => {
    setSuccessMessage('');
    setErrorMessage('');
    dispatch(clearPropertyError());
    dispatch(clearPropertySuccess());
  }, [dispatch, propertyData]);

  // Show success message when the API call succeeds
  useEffect(() => {
    if (success) {
      const message = propertyData ? 'Cập nhật tin đăng thành công!' : 'Tạo tin đăng mới thành công!';
      setSuccessMessage(message);
      
      // Clear success message after 5 seconds
      const timer = setTimeout(() => {
        setSuccessMessage('');
        dispatch(clearPropertySuccess());
      }, 5000);
      
      return () => clearTimeout(timer);
    }
  }, [success, propertyData, dispatch]);

  // Show error message when the API call fails
  useEffect(() => {
    if (error) {
      setErrorMessage(typeof error === 'string' ? error : 'Đã xảy ra lỗi khi xử lý yêu cầu');
    }
  }, [error]);

  // Nếu là chỉnh sửa, điền dữ liệu vào form
  useEffect(() => {
    if (propertyData) {
      setFormData({
        title: propertyData.title || '',
        description: propertyData.description || '',
        price: propertyData.price || '',
        address: propertyData.address || '',
        city: propertyData.city || '',
        district: propertyData.district || '',
        zipCode: propertyData.zipCode || '',
        type: propertyData.type || '',
        bedrooms: propertyData.bedrooms || '',
        bathrooms: propertyData.bathrooms || '',
        sqft: propertyData.sqft || '',
        yearBuilt: propertyData.yearBuilt || '',
        forSale: propertyData.forSale ?? true,
        forRent: propertyData.forRent ?? false,
        features: propertyData.features || [],
        categoryId: propertyData.categoryId || 1, // Use existing or default to 1
        userId: user?.id || 0,
        status: propertyData.status || 'PENDING',
        latitude: propertyData.latitude || 0,
        longitude: propertyData.longitude || 0,
        lotSize: propertyData.lotSize || '',
        daysOnMarket: propertyData.daysOnMarket || 0,
        street: propertyData.street || '',
        state: propertyData.state || '',
        // Agent info
        agentName: propertyData.agentName || user?.name || '',
        agentPhone: propertyData.agentPhone || user?.phone || '',
        agentEmail: propertyData.agentEmail || user?.email || '',
        agentImage: propertyData.agentImage || '',
        agentCompany: propertyData.agentCompany || '',
        // Neighborhood
        neighborhoodName: propertyData.neighborhoodName || '',
        neighborhoodDescription: propertyData.neighborhoodDescription || '',
        neighborhoodAmenities: propertyData.neighborhoodAmenities || [],
        walkScore: propertyData.walkScore || 0,
        transitScore: propertyData.transitScore || 0,
        bikeScore: propertyData.bikeScore || 0,
      });

      // Nếu có hình ảnh, hiển thị preview
      if (propertyData.images && propertyData.images.length > 0) {
        setPreviewImages(propertyData.images.map(url => ({ url, isUploaded: true })));
      }
    } else {
      // Reset form khi tạo mới
      setFormData({
        title: '',
        description: '',
        price: '',
        address: '',
        city: '',
        district: '',
        zipCode: '',
        type: '',
        bedrooms: '',
        bathrooms: '',
        sqft: '',
        yearBuilt: '',
        forSale: true,
        forRent: false,
        features: [],
        categoryId: 1, // Default category ID
        userId: user?.id || 0,
        status: 'PENDING',
        latitude: 0,
        longitude: 0,
        lotSize: '',
        daysOnMarket: 0,
        street: '',
        state: '',
        // Agent info
        agentName: user?.name || '',
        agentPhone: user?.phone || '',
        agentEmail: user?.email || '',
        agentImage: '',
        agentCompany: '',
        // Neighborhood
        neighborhoodName: '',
        neighborhoodDescription: '',
        neighborhoodAmenities: [],
        walkScore: 0,
        transitScore: 0,
        bikeScore: 0,
      });
      setPreviewImages([]);
      setUploadedImages([]);
    }
    setFormSubmitted(false);
  }, [propertyData, user]);

  // Xử lý thay đổi input
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    if (type === 'checkbox') {
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  // Xử lý thay đổi tính năng
  const handleFeatureChange = (featureId) => {
    setFormData(prev => {
      const features = [...prev.features];
      if (features.includes(featureId)) {
        return { ...prev, features: features.filter(id => id !== featureId) };
      } else {
        return { ...prev, features: [...features, featureId] };
      }
    });
  };

  // Xử lý tải lên hình ảnh
  const handleImageUpload = (e) => {
    const files = Array.from(e.target.files);
    if (files.length === 0) return;

    // Tạo URL cho preview
    const newPreviewImages = files.map(file => ({
      url: URL.createObjectURL(file),
      isUploaded: false
    }));

    setUploadedImages(prev => [...prev, ...files]);
    setPreviewImages(prev => [...prev, ...newPreviewImages]);
  };

  // Xóa hình ảnh
  const handleRemoveImage = (index) => {
    setPreviewImages(prev => prev.filter((_, i) => i !== index));
    setUploadedImages(prev => prev.filter((_, i) => i !== index));
  };

  // Hàm format giá tiền khi hiển thị
  const formatPrice = (price) => {
    if (!price) return '';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(price);
  };

  // Hàm xử lý khi giá trị thay đổi
  const handlePriceChange = (e) => {
    // Loại bỏ tất cả ký tự không phải số
    const rawValue = e.target.value.replace(/[^\d]/g, '');
    
    // Chuyển đổi thành số và cập nhật state nếu hợp lệ
    const numValue = Number(rawValue);
    if (!isNaN(numValue)) {
      setFormData({
        ...formData,
        price: numValue
      });
    }
  };

  // Gửi form
  const handleSubmit = (e) => {
    e.preventDefault();
    setFormSubmitted(true);

    // Kiểm tra dữ liệu
    if (!formData.title || !formData.price || !formData.address || !formData.type) {
      console.error("Validation failed: Missing required fields");
      return;
    }

    // Check if images are uploaded when creating a new property
    if (!propertyData?.id && previewImages.length === 0) {
      console.error("Validation failed: Images are required for new properties");
      return;
    }

    // Chuẩn bị dữ liệu để gửi
    const propertyToSubmit = {
      ...formData,
      price: parseFloat(formData.price),
      bedrooms: formData.bedrooms ? parseInt(formData.bedrooms) : 0,
      bathrooms: formData.bathrooms ? parseFloat(formData.bathrooms) : 0,
      sqft: formData.sqft ? parseFloat(formData.sqft) : 0,
      yearBuilt: formData.yearBuilt ? parseInt(formData.yearBuilt) : null,
      status: formData.status || 'PENDING', // Use the selected status
      categoryId: formData.categoryId || 1, // Ensure categoryId is included
      userId: user?.id || 0, // Ensure userId is included
      // Structure agent object
      agent: {
        name: formData.agentName || user?.name || '',
        phone: formData.agentPhone || user?.phone || '',
        email: formData.agentEmail || user?.email || '',
        image: formData.agentImage || '',
        company: formData.agentCompany || ''
      },
      // Structure location
      location: {
        lat: formData.latitude || 0,
        lng: formData.longitude || 0
      },
      // Structure address details
      addressDetails: {
        street: formData.street || '',
        city: formData.city || '',
        state: formData.state || '',
        zip: formData.zipCode || '',
        neighborhood: formData.neighborhoodName || ''
      },
      // Structure neighborhood
      neighborhood: {
        name: formData.neighborhoodName || '',
        description: formData.neighborhoodDescription || '',
        amenities: formData.neighborhoodAmenities || [],
        walkScore: formData.walkScore || 0,
        transitScore: formData.transitScore || 0,
        bikeScore: formData.bikeScore || 0
      }
    };

    console.log("Submitting property data:", propertyToSubmit);
    console.log("Images to upload:", uploadedImages.length);

    // Nếu có ID, cập nhật; nếu không, tạo mới
    if (propertyData?.id) {
      dispatch(updateProperty({ 
        id: propertyData.id, 
        propertyData: propertyToSubmit, 
        images: uploadedImages 
      }))
      .unwrap()
      .then(() => {
        // Success notification
        setSuccessMessage('Cập nhật tin đăng thành công!');
        // Reset form
        if (typeof window !== 'undefined') {
          window.scrollTo(0, 0);
        }
      })
      .catch((error) => {
        console.error('Error updating property:', error);
        setErrorMessage(`Lỗi khi cập nhật: ${error}`);
      });
    } else {
      dispatch(createProperty({ 
        propertyData: propertyToSubmit, 
        images: uploadedImages 
      }))
      .unwrap()
      .then((response) => {
        console.log("Property created successfully:", response);
        // Success notification
        setSuccessMessage('Tạo tin đăng thành công!');
        // Reset form
        setFormData({
          title: '',
          description: '',
          price: '',
          address: '',
          city: '',
          district: '',
          zipCode: '',
          type: '',
          bedrooms: '',
          bathrooms: '',
          sqft: '',
          yearBuilt: '',
          forSale: true,
          forRent: false,
          features: [],
          categoryId: 1,
          userId: user?.id || 0,
          status: 'PENDING',
          latitude: 0,
          longitude: 0,
          lotSize: '',
          daysOnMarket: 0,
          street: '',
          state: '',
          agentName: user?.name || '',
          agentPhone: user?.phone || '',
          agentEmail: user?.email || '',
          agentImage: '',
          agentCompany: '',
          neighborhoodName: '',
          neighborhoodDescription: '',
          neighborhoodAmenities: [],
          walkScore: 0,
          transitScore: 0,
          bikeScore: 0,
        });
        setFormSubmitted(false);
        setUploadedImages([]);
        setPreviewImages([]);
        if (typeof window !== 'undefined') {
          window.scrollTo(0, 0);
        }
      })
      .catch((error) => {
        console.error('Error creating property:', error);
        setErrorMessage(`Lỗi khi tạo tin đăng: ${JSON.stringify(error)}`);
      });
    }
  };

  return (
    <div className="space-y-6">
      <div className="pb-5 border-b border-gray-200">
        <h3 className="text-lg leading-6 font-medium text-gray-900">
          {propertyData ? 'Chỉnh sửa tin đăng' : 'Tạo tin đăng mới'}
        </h3>
        <p className="mt-1 text-sm text-gray-500">
          Vui lòng điền đầy đủ thông tin để tạo tin đăng bất động sản.
        </p>
        
        {/* Success message */}
        {successMessage && (
          <div className="mt-3 p-3 bg-green-50 text-green-800 rounded-md border border-green-200">
            <div className="flex">
              <svg className="h-5 w-5 text-green-400 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              <p>{successMessage}</p>
            </div>
          </div>
        )}
        
        {/* Error message */}
        {errorMessage && (
          <div className="mt-3 p-3 bg-red-50 text-red-800 rounded-md border border-red-200">
            <div className="flex">
              <svg className="h-5 w-5 text-red-400 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
              <p>{errorMessage}</p>
            </div>
          </div>
        )}
      </div>

      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Thông tin cơ bản */}
        <div className="bg-white shadow px-4 py-5 sm:rounded-lg sm:p-6">
          <div className="md:grid md:grid-cols-3 md:gap-6">
            <div className="md:col-span-1">
              <h3 className="text-lg font-medium leading-6 text-gray-900">Thông tin cơ bản</h3>
              <p className="mt-1 text-sm text-gray-500">
                Các thông tin cơ bản về bất động sản.
              </p>
            </div>
            <div className="mt-5 md:mt-0 md:col-span-2">
              <div className="grid grid-cols-6 gap-6">
                <div className="col-span-6">
                  <label htmlFor="title" className="block text-sm font-medium text-gray-700">
                    Tiêu đề <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="title"
                    id="title"
                    value={formData.title}
                    onChange={handleInputChange}
                    className={`mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md ${formSubmitted && !formData.title ? 'border-red-500' : ''}`}
                  />
                  {formSubmitted && !formData.title && (
                    <p className="mt-2 text-sm text-red-600">Vui lòng nhập tiêu đề</p>
                  )}
                </div>

                <div className="col-span-6">
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                    Mô tả
                  </label>
                  <textarea
                    id="description"
                    name="description"
                    rows={4}
                    value={formData.description}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  ></textarea>
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="price" className="block text-sm font-medium text-gray-700">
                    Giá (VNĐ) <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="price"
                    id="price"
                    value={formatPrice(formData.price)}
                    onChange={handlePriceChange}
                    className={`mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md ${formSubmitted && !formData.price ? 'border-red-500' : ''}`}
                  />
                  {formSubmitted && !formData.price && (
                    <p className="mt-2 text-sm text-red-600">Vui lòng nhập giá</p>
                  )}
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="type" className="block text-sm font-medium text-gray-700">
                    Loại bất động sản <span className="text-red-500">*</span>
                  </label>
                  <select
                    id="type"
                    name="type"
                    value={formData.type}
                    onChange={handleInputChange}
                    className={`mt-1 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm ${formSubmitted && !formData.type ? 'border-red-500' : ''}`}
                  >
                    {propertyTypes.map((type) => (
                      <option key={type.value} value={type.value}>
                        {type.label}
                      </option>
                    ))}
                  </select>
                  {formSubmitted && !formData.type && (
                    <p className="mt-2 text-sm text-red-600">Vui lòng chọn loại bất động sản</p>
                  )}
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="categoryId" className="block text-sm font-medium text-gray-700">
                    Danh mục bất động sản
                  </label>
                  <select
                    id="categoryId"
                    name="categoryId"
                    value={formData.categoryId}
                    onChange={handleInputChange}
                    className="mt-1 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                  >
                    {propertyCategories.map((category) => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="col-span-6 sm:col-span-2">
                  <label htmlFor="bedrooms" className="block text-sm font-medium text-gray-700">
                    Số phòng ngủ
                  </label>
                  <input
                    type="number"
                    name="bedrooms"
                    id="bedrooms"
                    min="0"
                    value={formData.bedrooms}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-2">
                  <label htmlFor="bathrooms" className="block text-sm font-medium text-gray-700">
                    Số phòng tắm
                  </label>
                  <input
                    type="number"
                    name="bathrooms"
                    id="bathrooms"
                    min="0"
                    step="0.5"
                    value={formData.bathrooms}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-2">
                  <label htmlFor="sqft" className="block text-sm font-medium text-gray-700">
                    Diện tích (m²)
                  </label>
                  <input
                    type="number"
                    name="sqft"
                    id="sqft"
                    min="0"
                    value={formData.sqft}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label className="block text-sm font-medium text-gray-700">Mục đích</label>
                  <div className="mt-4 space-x-4 flex items-center">
                    <div className="flex items-center">
                      <input
                        id="forSale"
                        name="forSale"
                        type="checkbox"
                        checked={formData.forSale}
                        onChange={handleInputChange}
                        className="h-4 w-4 text-rose-600 focus:ring-rose-500 border-gray-300 rounded"
                      />
                      <label htmlFor="forSale" className="ml-2 block text-sm text-gray-700">
                        Bán
                      </label>
                    </div>
                    <div className="flex items-center">
                      <input
                        id="forRent"
                        name="forRent"
                        type="checkbox"
                        checked={formData.forRent}
                        onChange={handleInputChange}
                        className="h-4 w-4 text-rose-600 focus:ring-rose-500 border-gray-300 rounded"
                      />
                      <label htmlFor="forRent" className="ml-2 block text-sm text-gray-700">
                        Cho thuê
                      </label>
                    </div>
                  </div>
                  {formSubmitted && !formData.forSale && !formData.forRent && (
                    <p className="mt-2 text-sm text-red-600">Vui lòng chọn ít nhất một mục đích</p>
                  )}
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="yearBuilt" className="block text-sm font-medium text-gray-700">
                    Năm xây dựng
                  </label>
                  <input
                    type="number"
                    name="yearBuilt"
                    id="yearBuilt"
                    min="1900"
                    max={new Date().getFullYear()}
                    value={formData.yearBuilt}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                {/* Trạng thái */}
                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="status" className="block text-sm font-medium text-gray-700">
                    Trạng thái
                  </label>
                  <select
                    id="status"
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                    className="mt-1 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm"
                  >
                    {propertyStatuses.map((status) => (
                      <option key={status.value} value={status.value}>
                        {status.label}
                      </option>
                    ))}
                  </select>
                  {formData.status === 'SOLD' && (
                    <p className="mt-1 text-sm text-gray-500">
                      Tin đăng đã bán sẽ được đánh dấu và hiển thị trạng thái "Đã bán" trên trang chi tiết.
                    </p>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Location and Map */}
        <div className="bg-white shadow px-4 py-5 sm:rounded-lg sm:p-6">
          <div className="md:grid md:grid-cols-3 md:gap-6">
            <div className="md:col-span-1">
              <h3 className="text-lg font-medium leading-6 text-gray-900">Vị trí bất động sản</h3>
              <p className="mt-1 text-sm text-gray-500">
                Chọn vị trí bất động sản của bạn trên bản đồ.
              </p>
            </div>
            <div className="mt-5 md:mt-0 md:col-span-2">
              <div className="h-80 w-full mb-4 rounded-md overflow-hidden">
                <PropertyMapForm 
                  formData={formData}
                  setFormData={setFormData}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="col-span-1">
                  <label htmlFor="latitude" className="block text-sm font-medium text-gray-700">
                    Vĩ độ (Latitude)
                  </label>
                  <input
                    type="number"
                    name="latitude"
                    id="latitude"
                    value={formData.latitude}
                    onChange={handleInputChange}
                    step="any"
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>
                <div className="col-span-1">
                  <label htmlFor="longitude" className="block text-sm font-medium text-gray-700">
                    Kinh độ (Longitude)
                  </label>
                  <input
                    type="number"
                    name="longitude"
                    id="longitude"
                    value={formData.longitude}
                    onChange={handleInputChange}
                    step="any"
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Địa chỉ chi tiết - Enhanced */}
        <div className="bg-white shadow px-4 py-5 sm:rounded-lg sm:p-6">
          <div className="md:grid md:grid-cols-3 md:gap-6">
            <div className="md:col-span-1">
              <h3 className="text-lg font-medium leading-6 text-gray-900">Địa chỉ chi tiết</h3>
              <p className="mt-1 text-sm text-gray-500">
                Thông tin chi tiết về địa chỉ bất động sản.
              </p>
            </div>
            <div className="mt-5 md:mt-0 md:col-span-2">
              <div className="grid grid-cols-6 gap-6">
                <div className="col-span-6">
                  <label htmlFor="street" className="block text-sm font-medium text-gray-700">
                    Tên đường / Số nhà
                  </label>
                  <input
                    type="text"
                    name="street"
                    id="street"
                    value={formData.street}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6">
                  <label htmlFor="address" className="block text-sm font-medium text-gray-700">
                    Địa chỉ đầy đủ <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="address"
                    id="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    className={`mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md ${formSubmitted && !formData.address ? 'border-red-500' : ''}`}
                  />
                  {formSubmitted && !formData.address && (
                    <p className="mt-2 text-sm text-red-600">Vui lòng nhập địa chỉ</p>
                  )}
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="city" className="block text-sm font-medium text-gray-700">
                    Thành phố / Tỉnh
                  </label>
                  <input
                    type="text"
                    name="city"
                    id="city"
                    value={formData.city}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="district" className="block text-sm font-medium text-gray-700">
                    Quận / Huyện
                  </label>
                  <input
                    type="text"
                    name="district"
                    id="district"
                    value={formData.district}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="state" className="block text-sm font-medium text-gray-700">
                    Tỉnh / Thành
                  </label>
                  <input
                    type="text"
                    name="state"
                    id="state"
                    value={formData.state}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-3">
                  <label htmlFor="zipCode" className="block text-sm font-medium text-gray-700">
                    Mã bưu chính
                  </label>
                  <input
                    type="text"
                    name="zipCode"
                    id="zipCode"
                    value={formData.zipCode}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Neighborhood Information */}
        <div className="bg-white shadow px-4 py-5 sm:rounded-lg sm:p-6">
          <div className="md:grid md:grid-cols-3 md:gap-6">
            <div className="md:col-span-1">
              <h3 className="text-lg font-medium leading-6 text-gray-900">Thông tin khu vực</h3>
              <p className="mt-1 text-sm text-gray-500">
                Mô tả về khu vực xung quanh bất động sản.
              </p>
            </div>
            <div className="mt-5 md:mt-0 md:col-span-2">
              <div className="grid grid-cols-6 gap-6">
                <div className="col-span-6">
                  <label htmlFor="neighborhoodName" className="block text-sm font-medium text-gray-700">
                    Tên khu vực
                  </label>
                  <input
                    type="text"
                    name="neighborhoodName"
                    id="neighborhoodName"
                    value={formData.neighborhoodName}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6">
                  <label htmlFor="neighborhoodDescription" className="block text-sm font-medium text-gray-700">
                    Mô tả khu vực
                  </label>
                  <textarea
                    name="neighborhoodDescription"
                    id="neighborhoodDescription"
                    rows={3}
                    value={formData.neighborhoodDescription}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-2">
                  <label htmlFor="walkScore" className="block text-sm font-medium text-gray-700">
                    Điểm đi bộ (0-100)
                  </label>
                  <input
                    type="number"
                    name="walkScore"
                    id="walkScore"
                    min="0"
                    max="100"
                    value={formData.walkScore}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-2">
                  <label htmlFor="transitScore" className="block text-sm font-medium text-gray-700">
                    Điểm giao thông (0-100)
                  </label>
                  <input
                    type="number"
                    name="transitScore"
                    id="transitScore"
                    min="0"
                    max="100"
                    value={formData.transitScore}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>

                <div className="col-span-6 sm:col-span-2">
                  <label htmlFor="bikeScore" className="block text-sm font-medium text-gray-700">
                    Điểm đạp xe (0-100)
                  </label>
                  <input
                    type="number"
                    name="bikeScore"
                    id="bikeScore"
                    min="0"
                    max="100"
                    value={formData.bikeScore}
                    onChange={handleInputChange}
                    className="mt-1 focus:ring-rose-500 focus:border-rose-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Tính năng */}
        <div className="bg-white shadow px-4 py-5 sm:rounded-lg sm:p-6">
          <div className="md:grid md:grid-cols-3 md:gap-6">
            <div className="md:col-span-1">
              <h3 className="text-lg font-medium leading-6 text-gray-900">Tính năng</h3>
              <p className="mt-1 text-sm text-gray-500">
                Chọn các tính năng có sẵn.
              </p>
            </div>
            <div className="mt-5 md:mt-0 md:col-span-2">
              <div className="grid grid-cols-2 gap-4">
                {availableFeatures.map((feature) => (
                  <div key={feature.id} className="flex items-center">
                    <input
                      id={feature.id}
                      name={feature.id}
                      type="checkbox"
                      checked={formData.features.includes(feature.id)}
                      onChange={() => handleFeatureChange(feature.id)}
                      className="h-4 w-4 text-rose-600 focus:ring-rose-500 border-gray-300 rounded"
                    />
                    <label htmlFor={feature.id} className="ml-2 block text-sm text-gray-700">
                      {feature.name}
                    </label>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Hình ảnh */}
        <div className="mt-10 border-t border-gray-200 pt-10">
          <h3 className="text-lg leading-6 font-medium text-gray-900">Hình ảnh bất động sản</h3>
          <p className="mt-1 text-sm text-gray-500">
            Tải lên hình ảnh chất lượng cao để thu hút người xem.
            {previewImages.length === 0 && <span className="text-red-500"> (Bắt buộc)*</span>}
          </p>

          <div className="mt-6">
            <div className="flex items-center">
              <div className="flex-grow">
                <label
                  htmlFor="image-upload"
                  className="block text-sm font-medium text-gray-700"
                >
                  Tải lên hình ảnh bất động sản
                </label>
                <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md">
                  <div className="space-y-1 text-center">
                    <svg
                      className="mx-auto h-12 w-12 text-gray-400"
                      stroke="currentColor"
                      fill="none"
                      viewBox="0 0 48 48"
                      aria-hidden="true"
                    >
                      <path
                        d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                        strokeWidth={2}
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                    <div className="flex text-sm text-gray-600">
                      <label
                        htmlFor="image-upload"
                        className="relative cursor-pointer bg-white rounded-md font-medium text-rose-600 hover:text-rose-500 focus-within:outline-none"
                      >
                        <span>Tải ảnh lên</span>
                        <input
                          id="image-upload"
                          name="image-upload"
                          type="file"
                          multiple
                          onChange={handleImageUpload}
                          className="sr-only"
                          accept="image/*"
                        />
                      </label>
                      <p className="pl-1">hoặc kéo thả vào đây</p>
                    </div>
                    <p className="text-xs text-gray-500">
                      PNG, JPG, GIF lên đến 10MB
                    </p>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Image previews */}
            {previewImages.length > 0 && (
              <div className="mt-4">
                <h4 className="text-sm font-medium text-gray-700 mb-2">
                  Hình ảnh đã tải lên ({previewImages.length})
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                  {previewImages.map((image, index) => (
                    <div key={index} className="relative group">
                      <div className="aspect-w-1 aspect-h-1 w-full overflow-hidden rounded-md bg-gray-200">
                        <img
                          src={image.url}
                          alt={`Preview ${index + 1}`}
                          className="h-full w-full object-cover object-center"
                        />
                      </div>
                      <button
                        type="button"
                        onClick={() => handleRemoveImage(index)}
                        className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <svg className="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            {formSubmitted && previewImages.length === 0 && (
              <p className="mt-2 text-sm text-red-600">
                Vui lòng tải lên ít nhất một hình ảnh
              </p>
            )}
          </div>
        </div>

        {/* Nút lưu */}
        <div className="px-4 py-3 bg-gray-50 text-right sm:px-6">
          <button
            type="submit"
            disabled={loading}
            className={`inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white ${
              loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500'
            }`}
          >
            {loading && (
              <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            )}
            {loading
              ? 'Đang xử lý...'
              : propertyData?.id
              ? 'Cập nhật tin đăng'
              : 'Tạo tin đăng mới'
            }
          </button>
        </div>
      </form>
    </div>
  );
};

export default PropertyForm; 