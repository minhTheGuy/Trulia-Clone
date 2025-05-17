import { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate, useSearchParams } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import './App.css'
import Header from './components/Header'
import Hero from './components/Hero'
import PropertyList from './components/PropertyList'
import FilterBar from './components/FilterBar'
import Footer from './components/Footer'
import PropertyDetails from './components/PropertyDetails'
import SignIn from './components/SignIn'
import SignUp from './components/SignUp'
import UserProfile from './components/UserProfile'
import { clearMessage } from './redux/slices/messageSlice'
import SellerDashboard from './components/seller/SellerDashboard'
import PropertyForm from './components/seller/PropertyForm'
import AdminDashboard from './components/admin/AdminDashboard'
import VerifyEmail from './components/VerifyEmail'
import ProtectedRoute from './components/ProtectedRoute'
import SavedHomes from './pages/SavedHomes'
import SavedSearches from './pages/SavedSearches'
import ForgotPassword from './components/ForgotPassword'
import ResetPassword from './components/ResetPassword'

export default function App() {
  const dispatch = useDispatch()
  const [filters, setFilters] = useState({
    type: 'all',
    priceRange: [0, 100000000000],
    bedrooms: 'any',
    bathrooms: 'any',
    sqftRange: [0, 1000],
    homeTypes: [],
    features: [],
    keyword: ''
  })

  useEffect(() => {
    dispatch(clearMessage())
  }, [dispatch])

  // HomePage component to combine Hero, FilterBar and PropertyList
  const HomePageComponent = () => {
    const [searchParams] = useSearchParams();
    const keyword = searchParams.get('keyword');
    const type = searchParams.get('type');
    const hasError = searchParams.get('error') === 'true';
    
    // Update filters based on URL search parameters
    useEffect(() => {
      if (keyword || type) {
        let updatedFilters = { ...filters };
        
        // Add keyword to filters if present
        if (keyword) {
          updatedFilters.keyword = keyword;
        }
        
        // Set property type based on search type
        if (type === 'buy') {
          updatedFilters.type = 'house';
        } else if (type === 'rent') {
          updatedFilters.type = 'apartment';
        } else if (type) {
          updatedFilters.type = type;
        }
        
        // Check if there are any changes to apply
        const hasChanges = 
          keyword !== filters.keyword ||
          updatedFilters.type !== filters.type;
        
        // Only update filters if there's an actual change
        if (hasChanges) {
          // Preserve existing filter values when updating
          setFilters(currentFilters => ({
            ...currentFilters,
            keyword: updatedFilters.keyword,
            type: updatedFilters.type
          }));
        }
      }
    }, [keyword, type]);
    
    return (
      <>
        <Hero />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <FilterBar filters={filters} setFilters={setFilters} />
          <PropertyList 
            filters={filters} 
            searchKeyword={keyword} 
            initialError={hasError} 
            initialFetch={!keyword && !type}
          />
        </div>
      </>
    )
  }

  // NotFound component for 404 errors
  const NotFound = () => (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-center">
      <h2 className="text-2xl font-bold text-gray-900 mb-4">Không Tìm Thấy Trang</h2>
      <p className="text-gray-700 mb-6">Trang bạn đang tìm kiếm không tồn tại.</p>
      <button
        onClick={() => window.location.href = '/'}
        className="inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-rose-600 hover:bg-rose-700"
      >
        Về Trang Chủ
      </button>
    </div>
  )

  // Layout component to wrap routes that need Header and Footer
  const Layout = ({ children }) => (
    <>
      <Header />
      <main className="flex-grow">
        {children}
      </main>
      <Footer />
    </>
  )

  return (
    <BrowserRouter>
      <div className="min-h-screen flex flex-col">
        <Routes>
          {/* Auth routes without header/footer */}
          <Route path="/signin" element={<SignIn />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/verify" element={<VerifyEmail />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password/:token" element={<ResetPassword />} />
          
          {/* Main routes with header/footer */}
          <Route path="/" element={<Layout><HomePageComponent /></Layout>} />
          <Route path="/property/:id" element={<Layout><PropertyDetails /></Layout>} />
          
          {/* Protected routes WITH layout */}
          <Route path="/profile" element={
            <ProtectedRoute>
              <Layout><UserProfile /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/settings" element={
            <ProtectedRoute>
              <Layout><UserProfile /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <Layout><SellerDashboard /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/admin" element={
            <ProtectedRoute>
              <Layout><AdminDashboard /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/add-property" element={
            <ProtectedRoute>
              <Layout><PropertyForm /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/edit-property/:id" element={
            <ProtectedRoute>
              <Layout><PropertyForm /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/my-properties" element={
            <ProtectedRoute>
              <Layout><SellerDashboard /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/saved-homes" element={
            <ProtectedRoute>
              <Layout><SavedHomes /></Layout>
            </ProtectedRoute>
          } />
          <Route path="/saved-searches" element={
            <ProtectedRoute>
              <Layout><SavedSearches /></Layout>
            </ProtectedRoute>
          } />

          {/* 404 route */}
          <Route path="/404" element={<Layout><NotFound /></Layout>} />
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}