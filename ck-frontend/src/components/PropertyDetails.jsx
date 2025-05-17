import React, { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { getPropertyById } from '../redux/slices/propertySlice'
import { fetchUserById } from '../redux/slices/userSlice'
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import ScheduleTour from './ScheduleTour'
import RequestInfo from './RequestInfo'
import SavedHomesButton from '../components/SavedHomesButton'
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js'
import { toast } from 'react-toastify'
import propertyService from '../services/propertyService'
import rentalService from '../services/rentalService'
import paymentService from '../services/paymentService'
import transactionService from '../services/transactionService'
import { loadStripe } from '@stripe/stripe-js'
import STRIPE_CONFIG from '../config/stripe'

// Fix Leaflet default marker icon issue
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png'
});

// Khởi tạo Stripe với publishable key
const stripePromise = loadStripe(STRIPE_CONFIG.PUBLISHABLE_KEY);

// Component thanh toán với Stripe
const RentalPaymentForm = ({ property, onSuccess }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [rentalPeriod, setRentalPeriod] = useState('1');
  const [rentalStartDate, setRentalStartDate] = useState('');
  const [rentalEndDate, setRentalEndDate] = useState('');
  const [termsAccepted, setTermsAccepted] = useState(false);
  // Get the current user from Redux state
  const { user } = useSelector(state => state.auth);

  useEffect(() => {
    // Set default start date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    setRentalStartDate(tomorrow.toISOString().split('T')[0]);
    
    // Calculate end date based on rental period
    const endDate = new Date(tomorrow);
    endDate.setMonth(endDate.getMonth() + parseInt(rentalPeriod));
    setRentalEndDate(endDate.toISOString().split('T')[0]);
  }, []);

  // Update end date when rental period or start date changes
  useEffect(() => {
    if (rentalStartDate) {
      const startDate = new Date(rentalStartDate);
      const endDate = new Date(startDate);
      endDate.setMonth(endDate.getMonth() + parseInt(rentalPeriod));
      setRentalEndDate(endDate.toISOString().split('T')[0]);
    }
  }, [rentalPeriod, rentalStartDate]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!stripe || !elements || !termsAccepted) {
      if (!termsAccepted) setError("Vui lòng đồng ý với các điều khoản và điều kiện thuê nhà");
      return;
    }
    setLoading(true);
    setError(null);

    try {
      // 1. Tạo payment method với Stripe
      const { error: stripeError, paymentMethod } = await stripe.createPaymentMethod({
        type: 'card',
        card: elements.getElement(CardElement),
      });
      
      if (stripeError) {
        setError(stripeError.message);
        setLoading(false);
        return;
      }

      // 2. Tạo transaction trước
      const transactionData = {
        propertyId: property.id,
        userId: user.id,
        sellerId: property.userId,
        amount: calculateTotalAmount(),
        rentalPeriod,
        rentalStartDate,
        rentalEndDate
      };
      
      const transaction = await transactionService.createRentalTransaction(transactionData);
      const transactionId = transaction.id || transaction.transactionId;
      
      if (!transactionId) {
        throw new Error('Không nhận được ID giao dịch');
      }

      // 3. Thực hiện thanh toán
      const paymentData = {
        payment_method_id: paymentMethod.id,
        transaction_id: transactionId,
        amount: calculateTotalAmount(),
        property_id: property.id,
        rental_period: rentalPeriod,
        rental_start_date: rentalStartDate,
        rental_end_date: rentalEndDate,
        user_id: user.id,
        seller_id: property.userId
      };
      
      const result = await paymentService.processRentalPayment(paymentData);

      // 4. Lưu thông tin thuê nhà nếu thanh toán thành công
      await rentalService.rentProperty({
        propertyId: property.id,
        userId: user.id,
        sellerId: property.userId,
        rentalPeriod,
        rentalStartDate,
        rentalEndDate,
        amount: calculateTotalAmount(),
        paymentId: result.paymentId || result.id,
        transactionId
      });

      onSuccess(result);
    } catch (error) {
      console.error('Payment error:', error);
      setError(error.message || 'Không thể xử lý thanh toán. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  const calculateTotalAmount = () => {
    const monthlyRent = property.rentPrice || property.price;
    return monthlyRent * parseInt(rentalPeriod);
  };

  return (
    <form onSubmit={handleSubmit} className="mt-4 space-y-4">
      <h3 className="text-lg font-medium text-gray-900">Đăng ký thuê nhà</h3>

      <div className="space-y-3">
        <div>
          <label htmlFor="rentalPeriod" className="block text-sm font-medium text-gray-700">
            Thời hạn thuê
          </label>
          <select
            id="rentalPeriod"
            name="rentalPeriod"
            value={rentalPeriod}
            onChange={(e) => setRentalPeriod(e.target.value)}
            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm rounded-md"
          >
            <option value="1">1 tháng</option>
            <option value="3">3 tháng</option>
            <option value="6">6 tháng</option>
            <option value="12">12 tháng</option>
          </select>
        </div>

        <div>
          <label htmlFor="rentalStartDate" className="block text-sm font-medium text-gray-700">
            Ngày bắt đầu thuê
          </label>
          <input
            type="date"
            id="rentalStartDate"
            name="rentalStartDate"
            value={rentalStartDate}
            onChange={(e) => setRentalStartDate(e.target.value)}
            min={new Date().toISOString().split('T')[0]}
            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-rose-500 focus:border-rose-500 sm:text-sm rounded-md"
          />
        </div>

        <div>
          <label htmlFor="rentalEndDate" className="block text-sm font-medium text-gray-700">
            Ngày kết thúc thuê
          </label>
          <input
            type="date"
            id="rentalEndDate"
            name="rentalEndDate"
            value={rentalEndDate}
            disabled
            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 bg-gray-50 sm:text-sm rounded-md"
          />
        </div>
      </div>

      <div className="bg-gray-50 p-4 rounded-md">
        <h4 className="text-sm font-medium text-gray-700 mb-2">Thông tin thanh toán</h4>
        <div className="flex justify-between mb-2">
          <span className="text-sm text-gray-500">Giá thuê hàng tháng:</span>
          <span className="text-sm font-medium">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(property.rentPrice || property.price)}</span>
        </div>
        <div className="flex justify-between mb-2">
          <span className="text-sm text-gray-500">Thời hạn thuê:</span>
          <span className="text-sm font-medium">{rentalPeriod} tháng</span>
        </div>
        <div className="flex justify-between font-medium">
          <span className="text-gray-700">Tổng số tiền:</span>
          <span className="text-rose-600">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(calculateTotalAmount())}</span>
        </div>
      </div>

      <div className="p-3 border border-gray-300 rounded-md">
        <CardElement options={{
          style: {
            base: {
              fontSize: '16px',
              color: '#424770',
              '::placeholder': {
                color: '#aab7c4',
              },
            },
            invalid: {
              color: '#9e2146',
            },
          },
        }} />
      </div>
      
      <div className="flex items-start">
        <div className="flex items-center h-5">
          <input
            id="terms"
            name="terms"
            type="checkbox"
            checked={termsAccepted}
            onChange={(e) => setTermsAccepted(e.target.checked)}
            className="focus:ring-rose-500 h-4 w-4 text-rose-600 border-gray-300 rounded"
          />
        </div>
        <div className="ml-3 text-sm">
          <label htmlFor="terms" className="font-medium text-gray-700">
            Tôi đồng ý với <a href="#" className="text-rose-600 hover:text-rose-500">điều khoản và điều kiện thuê nhà</a>
          </label>
        </div>
      </div>
      
      {error && (
        <div className="text-red-500 text-sm">{error}</div>
      )}
      
      <button
        type="submit"
        disabled={!stripe || loading || !termsAccepted}
        className={`w-full bg-rose-600 hover:bg-rose-700 text-white font-bold py-3 px-4 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500 ${(loading || !termsAccepted) ? 'opacity-70 cursor-not-allowed' : ''}`}
      >
        {loading ? 'Đang xử lý...' : `Thanh toán ${new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(calculateTotalAmount())}`}
      </button>
    </form>
  );
};

const PropertyDetails = () => {
  const [activeTab, setActiveTab] = useState('overview')
  const { id } = useParams()
  const navigate = useNavigate()
  const dispatch = useDispatch()
  
  // Get property from Redux store
  const { selectedProperty, loading, error } = useSelector(state => state.properties)
  const { userDetails } = useSelector(state => state.users)
  const [mainImage, setMainImage] = useState('')
  const [mapReady, setMapReady] = useState(false)
  // Change to true when direct payment is needed instead of the rental form
  const [showPaymentForm, setShowPaymentForm] = useState(false); 
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [tourScheduled, setTourScheduled] = useState(false);
  const [infoRequested, setInfoRequested] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showRentalForm, setShowRentalForm] = useState(false);
  const [rentalSuccess, setRentalSuccess] = useState(false);
  const { user } = useSelector(state => state.auth);

  // Effect for fetching property data based on ID
  useEffect(() => {
    if (id) {
      dispatch(getPropertyById(parseInt(id)));
    }
  }, [id, dispatch]);

  // Effect for updating UI elements like mainImage or fetching related data like userDetails
  useEffect(() => {
    if (selectedProperty && selectedProperty.id === parseInt(id)) {
      if (selectedProperty.images && selectedProperty.images.length > 0) {
        const currentMainImageStillValid = selectedProperty.images.includes(mainImage);
        if (!mainImage || !currentMainImageStillValid) {
            setMainImage(selectedProperty.images[0]);
        }
      } else {
        setMainImage(''); 
      }

      if (selectedProperty.userId) {
        dispatch(fetchUserById(selectedProperty.userId));
      }
    } else if (!selectedProperty && id) {
        setMainImage('');
    }
  }, [selectedProperty, id, dispatch, mainImage]);

  // Set map as ready after component mount
  useEffect(() => {
    setMapReady(true)
  }, [])

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(price)
  }

  // Determine if the property is sold
  const isPropertySold = selectedProperty?.status === 'SOLD' || selectedProperty?.status === 'sold';

  const handlePaymentSuccess = (paymentResult) => {
    // Store payment result details if needed
    console.log('Payment successful:', paymentResult);
    setPaymentSuccess(true);
    setShowPaymentForm(false);
    setRentalSuccess(true);
    // Optionally, update property status to RENTED
    toast.success("Giao dịch thuê nhà thành công!");
  };

  const handleTourSchedule = async (tourData) => {
    if (!user) {
      toast.error("Please login to schedule a tour");
      return;
    }
    
    setIsSubmitting(true);
    try {
      await propertyService.scheduleTour({
        propertyId: selectedProperty.id,
        userId: user.id,
        ...tourData
      });
      
      setTourScheduled(true);
      toast.success("Tour scheduled successfully!");
    } catch (error) {
      console.error('Error scheduling tour:', error);
      toast.error(typeof error === 'string' ? error : "Failed to schedule tour. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInfoRequest = async (requestData) => {
    if (!user) {
      toast.error("Please login to request information");
      return;
    }
    
    setIsSubmitting(true);
    try {
      await propertyService.requestInfo({
        propertyId: selectedProperty.id,
        userId: user.id,
        sellerUserId: selectedProperty.userId,
        ...requestData
      });
      
      setInfoRequested(true);
      toast.success("Information requested successfully!");
    } catch (error) {
      console.error('Error requesting information:', error);
      toast.error(typeof error === 'string' ? error : "Failed to request information. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Loading property details...</h2>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Error loading property</h2>
        <p className="text-gray-700 mb-6">{error}</p>
        <button
          onClick={() => navigate('/')}
          className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700"
        >
          Go Back Home
        </button>
      </div>
    )
  }

  if (!selectedProperty) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Property Not Found</h2>
        <p className="text-gray-700 mb-6">The property you're looking for doesn't exist or has been removed.</p>
        <button
          onClick={() => navigate('/')}
          className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700"
        >
          Go Back Home
        </button>
      </div>
    )
  }

  // Prepare user info for display
  const userInfo = userDetails || {
    username: selectedProperty.agent?.name || 'Không xác định',
    email: selectedProperty.agent?.email || '',
    phone: selectedProperty.agent?.phone || '',
    image: selectedProperty.agent?.image || 'https://via.placeholder.com/150'
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back button */}
      <div className="mb-6">
        <Link 
          to="/"
          className="text-rose-600 hover:text-rose-700 flex items-center"
        >
          <svg
            className="h-5 w-5 mr-1"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to search results
        </Link>
      </div>

      {/* Property title and address */}
      <div className="flex justify-between items-start mb-6">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">{selectedProperty.title}</h1>
            {user && <SavedHomesButton property={selectedProperty} />}
          </div>
          <p className="text-xl text-gray-600">{selectedProperty.address}</p>
          
          {/* Property Status Badge */}
          <div className="mt-2">
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
              ${selectedProperty.status === 'published' || selectedProperty.status === 'available' 
                ? 'bg-green-100 text-green-800' 
                : selectedProperty.status === 'PENDING' 
                  ? 'bg-yellow-100 text-yellow-800'
                  : selectedProperty.status === 'SOLD' || selectedProperty.status === 'RENTED'
                    ? 'bg-blue-100 text-blue-800'
                    : 'bg-red-100 text-red-800'
              }`}>
              {selectedProperty.status === 'published' ? 'Đang đăng bán' : 
               selectedProperty.status === 'AVAILABLE' ? 'Đang có sẵn' : 
               selectedProperty.status === 'PENDING' ? 'Đang chờ xét duyệt' : 
               selectedProperty.status === 'SOLD' ? 'Đã bán' :
               selectedProperty.status === 'RENTED' ? 'Đã cho thuê' :
               selectedProperty.status || 'Không xác định'}
            </span>
          </div>
        </div>
      </div>

      {/* Property images */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="md:col-span-2">
          <img src={mainImage} alt={selectedProperty.title} className="rounded-lg w-full h-[400px] object-cover" />
        </div>
        <div className="grid grid-cols-2 gap-2 h-[400px] overflow-y-auto">
          {selectedProperty.images && selectedProperty.images.map((image, index) => (
            <img
              key={index}
              src={image}
              alt={`${selectedProperty.title} - Image ${index + 1}`}
              className={`rounded-lg w-full h-48 object-cover cursor-pointer ${
                mainImage === image ? 'border-2 border-rose-600' : ''
              }`}
              onClick={() => setMainImage(image)}
            />
          ))}
        </div>
      </div>

      {/* Main content area - 2 columns on desktop */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
        {/* Left column - Property details */}
        <div className="lg:col-span-2">
          {/* Price and key details */}
          <div className="flex flex-wrap items-center gap-6 mb-6">
            {isPropertySold ? (
              <div className="px-4 py-2 bg-blue-100 text-blue-800 rounded-md flex items-center font-bold text-xl">
                <svg className="h-6 w-6 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Đã bán
              </div>
            ) : (
              <div>
                <span className="text-3xl font-bold text-gray-900">{formatPrice(selectedProperty.price)}</span>
                {selectedProperty.forRent && <span className="text-gray-500 ml-1">/tháng</span>}
              </div>
            )}
            <div className="flex items-center">
              <svg
                className="h-5 w-5 text-gray-400 mr-1"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                />
              </svg>
              <span className="text-gray-700">{selectedProperty.sqft} m²</span>
            </div>
            <div className="flex items-center">
              <svg
                className="h-5 w-5 text-gray-400 mr-1"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                />
              </svg>
              <span className="text-gray-700">{selectedProperty.bedrooms} Phòng ngủ</span>
            </div>
            <div className="flex items-center">
              <svg
                className="h-5 w-5 text-gray-400 mr-1"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <span className="text-gray-700">{selectedProperty.bathrooms} Phòng tắm</span>
            </div>
          </div>

          {/* Tabs */}
          <div className="border-b border-gray-200 mb-8">
            <nav className="-mb-px flex space-x-8">
              <button
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'overview'
                    ? 'border-rose-500 text-rose-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
                onClick={() => setActiveTab('overview')}
              >
                Overview
              </button>
              <button
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'features'
                    ? 'border-rose-500 text-rose-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
                onClick={() => setActiveTab('features')}
              >
                Features
              </button>
              <button
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'map'
                    ? 'border-rose-500 text-rose-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
                onClick={() => setActiveTab('map')}
              >
                Map
              </button>
              <button
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'neighborhood'
                    ? 'border-rose-500 text-rose-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
                onClick={() => setActiveTab('neighborhood')}
              >
                Neighborhood
              </button>
              <button
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'contact'
                    ? 'border-rose-500 text-rose-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
                onClick={() => setActiveTab('contact')}
              >
                Contact
              </button>
            </nav>
          </div>

          {/* Tab Content */}
          <div className="mb-8">
            {/* Overview Tab */}
            {activeTab === 'overview' && (
              <div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                  <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">Property Details</h3>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm text-gray-500">Type</p>
                        <p className="text-sm font-medium text-gray-900">{selectedProperty.type}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Year Built</p>
                        <p className="text-sm font-medium text-gray-900">{selectedProperty.yearBuilt}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Square Feet</p>
                        <p className="text-sm font-medium text-gray-900">{selectedProperty.sqft}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Lot Size</p>
                        <p className="text-sm font-medium text-gray-900">{selectedProperty.lotSize}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Bedrooms</p>
                        <p className="text-sm font-medium text-gray-900">{selectedProperty.bedrooms}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Bathrooms</p>
                        <p className="text-sm font-medium text-gray-900">{selectedProperty.bathrooms}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Status</p>
                        <p className="text-sm font-medium text-gray-900 capitalize">
                          {selectedProperty.status?.replace(/_/g, ' ')}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-500">Days on Market</p>
                        <p className="text-sm font-medium text-gray-900">{selectedProperty.daysOnMarket}</p>
                      </div>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-2">Người đăng bài</h3>
                    <div className="flex items-start">
                      <img
                        src={userInfo.image}
                        alt={userInfo.username}
                        className="h-16 w-16 rounded-full object-cover mr-4"
                      />
                      <div>
                        <p className="font-medium text-gray-900">{userInfo.username}</p>
                        {userInfo.email && <p className="text-sm text-gray-500">{userInfo.email}</p>}
                        {userInfo.phone && <p className="text-sm text-gray-500">{userInfo.phone}</p>}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="mb-6">
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Description</h3>
                  <p className="text-gray-700 whitespace-pre-line">{selectedProperty.description}</p>
                </div>
              </div>
            )}

            {/* Features Tab */}
            {activeTab === 'features' && (
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">Property Features</h3>
                <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {selectedProperty.features && selectedProperty.features.map((feature, index) => (
                    <li key={index} className="flex items-center">
                      <svg
                        className="h-5 w-5 text-rose-500 mr-2"
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                      <span className="text-gray-700">{feature}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Map Tab */}
            {activeTab === 'map' && mapReady && selectedProperty.location && (
              <div className="h-[500px]">
                    <MapContainer 
                      center={[selectedProperty.location.lat, selectedProperty.location.lng]} 
                      zoom={14} 
                      style={{ height: '100%', width: '100%' }}
                  scrollWheelZoom={false}
                    >
                      <TileLayer
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                      />
                      <Marker position={[selectedProperty.location.lat, selectedProperty.location.lng]}>
                        <Popup>
                          <div>
                        <h3 className="font-medium">{selectedProperty.title}</h3>
                        <p className="text-sm">{selectedProperty.address}</p>
                        <p className="text-sm font-medium">{formatPrice(selectedProperty.price)}</p>
                          </div>
                        </Popup>
                      </Marker>
                    </MapContainer>
              </div>
            )}

            {/* Neighborhood Tab */}
            {activeTab === 'neighborhood' && (
              <div>
                <div className="mb-6">
                  <h3 className="text-lg font-medium text-gray-900 mb-2">About the Neighborhood</h3>
                  {selectedProperty.neighborhood && (
                    <>
                      <h4 className="font-medium text-gray-800 mb-1">{selectedProperty.neighborhood.name}</h4>
                      <p className="text-gray-700 mb-4">{selectedProperty.neighborhood.description}</p>
                      
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                        <div className="bg-gray-50 p-4 rounded-lg">
                          <div className="flex items-center mb-2">
                            <svg
                              className="h-5 w-5 text-gray-900 mr-2"
                              xmlns="http://www.w3.org/2000/svg"
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"
                              />
                            </svg>
                            <span className="font-medium text-gray-800">Walk Score</span>
                          </div>
                          <div className="flex items-center">
                            <div className="w-full bg-gray-200 rounded-full h-2.5">
                              <div
                                className="bg-rose-600 h-2.5 rounded-full"
                                style={{ width: `${selectedProperty.neighborhood.walkScore}%` }}
                              ></div>
                            </div>
                            <span className="ml-2 text-gray-700">{selectedProperty.neighborhood.walkScore}/100</span>
                          </div>
                        </div>
                        
                        <div className="bg-gray-50 p-4 rounded-lg">
                          <div className="flex items-center mb-2">
                            <svg
                              className="h-5 w-5 text-gray-900 mr-2"
                              xmlns="http://www.w3.org/2000/svg"
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2"
                              />
                            </svg>
                            <span className="font-medium text-gray-800">Transit Score</span>
                          </div>
                          <div className="flex items-center">
                            <div className="w-full bg-gray-200 rounded-full h-2.5">
                              <div
                                className="bg-rose-600 h-2.5 rounded-full"
                                style={{ width: `${selectedProperty.neighborhood.transitScore}%` }}
                              ></div>
                            </div>
                            <span className="ml-2 text-gray-700">{selectedProperty.neighborhood.transitScore}/100</span>
                          </div>
                        </div>
                        
                        <div className="bg-gray-50 p-4 rounded-lg">
                          <div className="flex items-center mb-2">
                            <svg
                              className="h-5 w-5 text-gray-900 mr-2"
                              xmlns="http://www.w3.org/2000/svg"
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"
                              />
                            </svg>
                            <span className="font-medium text-gray-800">Bike Score</span>
                          </div>
                          <div className="flex items-center">
                            <div className="w-full bg-gray-200 rounded-full h-2.5">
                              <div
                                className="bg-rose-600 h-2.5 rounded-full"
                                style={{ width: `${selectedProperty.neighborhood.bikeScore}%` }}
                              ></div>
                            </div>
                            <span className="ml-2 text-gray-700">{selectedProperty.neighborhood.bikeScore}/100</span>
                          </div>
                        </div>
                      </div>
                    </>
                  )}
                </div>
                
                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Nearby Amenities</h3>
                  <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {selectedProperty.neighborhood && selectedProperty.neighborhood.amenities && selectedProperty.neighborhood.amenities.map((amenity, index) => (
                      <li key={index} className="flex items-center">
                        <svg
                          className="h-5 w-5 text-rose-500 mr-2"
                          xmlns="http://www.w3.org/2000/svg"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                          />
                        </svg>
                        <span className="text-gray-700">{amenity}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}

            {/* Contact Tab */}
            {activeTab === 'contact' && (
              <div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  {/* Schedule Tour */}
                  <div className="mb-8 md:mb-0">
                    {tourScheduled ? (
                      <div className="p-4 bg-green-50 rounded-md">
                        <div className="flex items-center">
                          <svg className="h-5 w-5 text-green-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
                          </svg>
                          <p className="text-green-800 font-medium">Lịch tham quan đã được đặt!</p>
                        </div>
                        <p className="text-sm text-green-700 mt-1">Chúng tôi sẽ liên hệ với bạn sớm để xác nhận.</p>
                      </div>
                    ) : (
                      <ScheduleTour 
                        propertyId={selectedProperty.id} 
                        sellerInfo={{
                          id: selectedProperty.userId,
                          name: userInfo.username,
                          email: userInfo.email,
                          phone: userInfo.phone,
                          image: userInfo.image,
                        }}
                        onSubmit={handleTourSchedule}
                        isSubmitting={isSubmitting}
                      />
                    )}
                  </div>
                  
                  {/* Request Info */}
                  <div>
                    {infoRequested ? (
                      <div className="p-4 bg-green-50 rounded-md">
                        <div className="flex items-center">
                          <svg className="h-5 w-5 text-green-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
                          </svg>
                          <p className="text-green-800 font-medium">Yêu cầu thông tin đã được gửi!</p>
                        </div>
                        <p className="text-sm text-green-700 mt-1">Người đăng bài sẽ liên hệ lại với bạn sớm.</p>
                      </div>
                    ) : (
                      <RequestInfo 
                        propertyId={selectedProperty.id}
                        propertyTitle={selectedProperty.title}
                        onSubmit={handleInfoRequest}
                        isSubmitting={isSubmitting}
                      />
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right column - Contact and Schedule */}
        <div className="lg:col-span-1 space-y-6">
          {/* Agent card */}
          <div className="bg-white shadow rounded-lg overflow-hidden border border-gray-200">
            <div className="p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-3">Thông tin liên hệ</h3>
              <div className="flex items-center mb-4">
                <img 
                  src={userInfo.image || "https://via.placeholder.com/150"}
                  alt={userInfo.username}
                  className="w-12 h-12 rounded-full object-cover mr-4"
                />
                <div>
                  <p className="font-medium text-gray-900">{userInfo.username}</p>
                  {userInfo.phone && <p className="text-sm text-gray-500">{userInfo.phone}</p>}
                </div>
              </div>
              
              {/* CTA Buttons */}
              <div className="space-y-3">
                {!isPropertySold && (
                  <>
                    {/* Show Rent button only for rental properties */}
                    {selectedProperty.forRent && (
                      <button
                        className="w-full flex justify-center items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-base font-medium text-white bg-blue-600 hover:bg-blue-700"
                        onClick={() => setShowRentalForm(true)}
                      >
                        <svg className="h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        Thuê nhà
                      </button>
                    )}

                    <button
                      className="w-full flex justify-center items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-base font-medium text-white bg-rose-600 hover:bg-rose-700"
                      onClick={() => setActiveTab('contact')}
                    >
                      <svg className="h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                      </svg>
                      Yêu cầu thông tin
                    </button>
                    
                    <button
                      className="w-full flex justify-center items-center px-4 py-2 border border-rose-600 rounded-md shadow-sm text-base font-medium text-rose-600 bg-white hover:bg-rose-50"
                      onClick={() => setActiveTab('contact')}
                    >
                      <svg className="h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      Đặt lịch xem nhà
                    </button>
                  </>
                )}
                
                <button
                  className="w-full flex justify-center items-center px-4 py-2 border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 rounded-md shadow-sm text-base font-medium"
                  onClick={() => window.location.href = `tel:${userInfo.phone}`}
                >
                  <svg className="h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                  Gọi điện thoại
                </button>

                <button
                  className="w-full flex justify-center items-center px-4 py-2 border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 rounded-md shadow-sm text-base font-medium"
                  onClick={() => window.location.href = `mailto:${userInfo.email}`}
                >
                  <svg className="h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                  Gửi email
                </button>
              </div>
            </div>
          </div>
          
          {/* Tình trạng bất động sản */}
          {isPropertySold && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex items-start">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-blue-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-blue-800">Bất động sản đã bán</h3>
                  <div className="mt-2 text-sm text-blue-700">
                    <p>Bất động sản này đã được bán và không còn khả dụng. Bạn có thể xem các bất động sản tương tự khác hoặc liên hệ với người bán để biết thêm thông tin.</p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Rental success message */}
          {rentalSuccess && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-4">
              <div className="flex items-start">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-green-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-green-800">Đã thuê nhà thành công</h3>
                  <div className="mt-2 text-sm text-green-700">
                    <p>Bạn đã đăng ký thuê nhà thành công. {paymentSuccess ? 'Thanh toán đã được xác nhận.' : ''} Chúng tôi sẽ liên hệ với bạn để hoàn tất thủ tục hợp đồng thuê nhà.</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Rental form modal */}
      {showRentalForm && (
        <div className="fixed z-10 inset-0 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div className="fixed inset-0 transition-opacity" aria-hidden="true">
              <div className="absolute inset-0 bg-gray-500 opacity-75"></div>
            </div>
            <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
            <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
              <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                <div className="sm:flex sm:items-start">
                  <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">
                      Đăng ký thuê nhà
                    </h3>
                    <div className="mt-4">
                      {user ? (
                        // Show direct payment form if showPaymentForm is true
                        showPaymentForm ? (
                          <div className="text-center py-4">
                            <p className="text-gray-600 mb-4">Đang xử lý thanh toán...</p>
                          </div>
                        ) : (
                          <Elements stripe={stripePromise}>
                            <RentalPaymentForm property={selectedProperty} onSuccess={handlePaymentSuccess} />
                          </Elements>
                        )
                      ) : (
                        <div className="text-center py-4">
                          <p className="text-gray-600 mb-4">Bạn cần đăng nhập để thuê nhà</p>
                          <Link
                            to="/signin"
                            className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700"
                          >
                            Đăng nhập
                          </Link>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={() => {
                    setShowRentalForm(false);
                    setShowPaymentForm(false);
                  }}
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default PropertyDetails 