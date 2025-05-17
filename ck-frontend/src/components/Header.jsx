import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { logout } from '../redux/slices/authSlice'

const Header = () => {
  const { isLoggedIn, user } = useSelector(state => state.auth)
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [activeDropdown, setActiveDropdown] = useState(null)

  // Kiểm tra vai trò người dùng
  const isSeller = user?.roles?.includes('ROLE_SELLER');
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  const dropdownMenus = {
    buy: [
      { title: 'Ashburn', href: '/buy/ashburn' },
      { title: 'Homes for Sale', href: '/buy/homes-for-sale' },
      { title: 'Open Houses', href: '/buy/open-houses' },
      { title: 'New Homes', href: '/buy/new-homes' },
      { title: 'Recently Sold', href: '/buy/recently-sold' }
    ],
    rent: [
      { title: 'Ashburn', href: '/rent/ashburn' },
      { title: 'All Rentals', href: '/rent/all-rentals' },
      { title: 'Apartments for Rent', href: '/rent/apartments' },
      { title: 'Houses for Rent', href: '/rent/houses' },
      { title: 'Post A Rental Listing', href: '/rent/post-listing' }
    ],
    mortgage: [
      { title: 'Ashburn', href: '/mortgage/ashburn' },
      { title: 'Mortgage Overview', href: '/mortgage/overview' },
      { title: 'Get Pre-Qualified', href: '/mortgage/pre-qualified' },
      { title: 'Mortgage Rates', href: '/mortgage/rates' },
      { title: 'Refinance Rates', href: '/mortgage/refinance-rates' },
      { title: 'Mortgage Calculator', href: '/mortgage/calculator' },
      { title: 'Affordability Calculator', href: '/mortgage/affordability' },
      { title: 'Refinance Calculator', href: '/mortgage/refinance-calculator' },
      { title: 'Rent vs Buy Calculator', href: '/mortgage/rent-vs-buy' }
    ],
    sell: [
      { title: 'Selling Options', href: '/sell/options' },
      { title: 'Home Values', href: '/sell/home-values' },
      { title: 'Home Selling Guide', href: '/sell/guide' },
      { title: 'Post For Sale By Owner', href: '/sell/for-sale-by-owner' }
    ],
    account: [
      { title: 'Thông tin tài khoản', href: '/profile' },
      ...(isAdmin ? [{ title: 'Quản trị hệ thống', href: '/admin' }] : []),
      ...(isSeller ? [{ title: 'Quản lý tin đăng', href: '/dashboard' }] : []),
      { title: 'Cài đặt', href: '/settings' },
      { title: 'Đăng xuất', action: 'logout' }
    ]
  }

  const handleDropdownToggle = (menu) => {
    if (activeDropdown === menu) {
      setActiveDropdown(null)
    } else {
      setActiveDropdown(menu)
    }
  }

  const closeDropdowns = () => {
    setActiveDropdown(null)
  }

  const handleLogout = () => {
    dispatch(logout())
    closeDropdowns()
    navigate('/')
  }

  return (
    <header className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Logo */}
          <div className="flex-shrink-0 flex items-center">
            <Link 
              to="/" 
              className="flex items-center cursor-pointer"
              onClick={closeDropdowns}
            >
              <span className="text-2xl font-bold text-rose-600">Trulia</span>
              <span className="ml-1 text-2xl font-bold text-gray-900">Clone</span>
            </Link>
          </div>

          {/* Main Navigation */}
          <div className="hidden md:ml-6 md:flex md:items-center md:space-x-4">
            <div className="relative">
              <button 
                className={`px-3 py-2 text-sm font-medium hover:text-rose-600 flex items-center ${activeDropdown === 'buy' ? 'text-rose-600' : 'text-gray-900'}`}
                onClick={() => handleDropdownToggle('buy')}
              >
                Buy
                <svg className={`ml-1 h-4 w-4 transition-transform ${activeDropdown === 'buy' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'buy' && (
                <div className="absolute left-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 ring-1 ring-black ring-opacity-5">
                  {dropdownMenus.buy.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      onClick={closeDropdowns}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <div className="relative">
              <button 
                className={`px-3 py-2 text-sm font-medium hover:text-rose-600 flex items-center ${activeDropdown === 'rent' ? 'text-rose-600' : 'text-gray-900'}`}
                onClick={() => handleDropdownToggle('rent')}
              >
                Rent
                <svg className={`ml-1 h-4 w-4 transition-transform ${activeDropdown === 'rent' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'rent' && (
                <div className="absolute left-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 ring-1 ring-black ring-opacity-5">
                  {dropdownMenus.rent.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      onClick={closeDropdowns}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <div className="relative">
              <button 
                className={`px-3 py-2 text-sm font-medium hover:text-rose-600 flex items-center ${activeDropdown === 'sell' ? 'text-rose-600' : 'text-gray-900'}`}
                onClick={() => handleDropdownToggle('sell')}
              >
                Sell
                <svg className={`ml-1 h-4 w-4 transition-transform ${activeDropdown === 'sell' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'sell' && (
                <div className="absolute left-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 ring-1 ring-black ring-opacity-5">
                  {dropdownMenus.sell.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      onClick={closeDropdowns}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <div className="relative">
              <button 
                className={`px-3 py-2 text-sm font-medium hover:text-rose-600 flex items-center ${activeDropdown === 'mortgage' ? 'text-rose-600' : 'text-gray-900'}`}
                onClick={() => handleDropdownToggle('mortgage')}
              >
                Mortgage
                <svg className={`ml-1 h-4 w-4 transition-transform ${activeDropdown === 'mortgage' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'mortgage' && (
                <div className="absolute left-0 mt-2 w-56 bg-white rounded-md shadow-lg py-1 z-50 ring-1 ring-black ring-opacity-5">
                  {dropdownMenus.mortgage.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      onClick={closeDropdowns}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* User account and mobile menu */}
          <div className="flex items-center">
            <div className="hidden md:ml-4 md:flex md:items-center">
              <Link
                to="/saved-homes"
                className="px-3 py-2 text-sm font-medium text-gray-900 hover:text-rose-600"
                onClick={closeDropdowns}
              >
                Saved Homes
              </Link>
              <Link
                to="/saved-searches"
                className="px-3 py-2 text-sm font-medium text-gray-900 hover:text-rose-600"
                onClick={closeDropdowns}
              >
                Saved Searches
              </Link>
              
              {/* Chế độ đăng nhập/đăng ký và chế độ đã đăng nhập */}
              {isLoggedIn ? (
                <div className="relative ml-4">
                  <button 
                    className="flex items-center px-3 py-2 text-sm font-medium text-gray-900 hover:text-rose-600"
                    onClick={() => handleDropdownToggle('account')}
                  >
                    <div className="w-8 h-8 rounded-full bg-rose-600 text-white flex items-center justify-center mr-2">
                      {user?.username?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <span>{user?.username || 'User'}</span>
                    <svg className={`ml-1 h-4 w-4 transition-transform ${activeDropdown === 'account' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                    </svg>
                  </button>
                  
                  {activeDropdown === 'account' && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 ring-1 ring-black ring-opacity-5">
                      {dropdownMenus.account.map((item, index) => (
                        item.action === 'logout' ? (
                          <button
                            key={index}
                            onClick={handleLogout}
                            className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                          >
                            {item.title}
                          </button>
                        ) : (
                          <Link
                            key={index}
                            to={item.href}
                            className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                            onClick={closeDropdowns}
                          >
                            {item.title}
                          </Link>
                        )
                      ))}
                    </div>
                  )}
                </div>
              ) : (
                <>
                  <Link
                    to="/signin"
                    className="px-3 py-2 text-sm font-medium text-gray-900 hover:text-rose-600"
                    onClick={closeDropdowns}
                  >
                    Sign In
                  </Link>
                  <Link
                    to="/signup"
                    className="ml-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-rose-600 hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
                    onClick={closeDropdowns}
                  >
                    Get Started
                  </Link>
                </>
              )}
            </div>

            {/* Mobile menu button */}
            <div className="flex md:hidden">
              <button
                type="button"
                className="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-rose-500"
                onClick={() => setIsMenuOpen(!isMenuOpen)}
              >
                <span className="sr-only">Open main menu</span>
                {!isMenuOpen ? (
                  <svg
                    className="block h-6 w-6"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    aria-hidden="true"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                ) : (
                  <svg
                    className="block h-6 w-6"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    aria-hidden="true"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Mobile menu, show/hide based on menu state */}
      {isMenuOpen && (
        <div className="md:hidden">
          <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
            <div className="block py-2">
              <button 
                className="flex items-center justify-between w-full px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                onClick={() => handleDropdownToggle('buy')}
              >
                <span>Buy</span>
                <svg className={`ml-1 h-5 w-5 transition-transform ${activeDropdown === 'buy' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'buy' && (
                <div className="pl-4 pr-2 py-2">
                  {dropdownMenus.buy.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <div className="block py-2">
              <button 
                className="flex items-center justify-between w-full px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                onClick={() => handleDropdownToggle('rent')}
              >
                <span>Rent</span>
                <svg className={`ml-1 h-5 w-5 transition-transform ${activeDropdown === 'rent' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'rent' && (
                <div className="pl-4 pr-2 py-2">
                  {dropdownMenus.rent.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <div className="block py-2">
              <button 
                className="flex items-center justify-between w-full px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                onClick={() => handleDropdownToggle('sell')}
              >
                <span>Sell</span>
                <svg className={`ml-1 h-5 w-5 transition-transform ${activeDropdown === 'sell' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'sell' && (
                <div className="pl-4 pr-2 py-2">
                  {dropdownMenus.sell.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <div className="block py-2">
              <button 
                className="flex items-center justify-between w-full px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                onClick={() => handleDropdownToggle('mortgage')}
              >
                <span>Mortgage</span>
                <svg className={`ml-1 h-5 w-5 transition-transform ${activeDropdown === 'mortgage' ? 'transform rotate-180' : ''}`} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
              
              {activeDropdown === 'mortgage' && (
                <div className="pl-4 pr-2 py-2">
                  {dropdownMenus.mortgage.map((item, index) => (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <Link 
              to="/agents"
              className="block px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
              onClick={() => setIsMenuOpen(false)}
            >
              Find Agents
            </Link>

            <Link 
              to="/saved-homes"
              className="block px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
              onClick={() => setIsMenuOpen(false)}
            >
              Saved Homes
            </Link>

            <Link 
              to="/saved-searches"
              className="block px-3 py-2 text-base font-medium text-gray-900 rounded-md hover:bg-gray-50"
              onClick={() => setIsMenuOpen(false)}
            >
              Saved Searches
            </Link>
          </div>
          <div className="pt-4 pb-3 border-t border-gray-200">
            <div className="flex items-center px-5">
              {isLoggedIn ? (
                <div className="flex items-center justify-between w-full">
                  <div className="flex items-center">
                    <div className="w-10 h-10 rounded-full bg-rose-600 text-white flex items-center justify-center mr-3">
                      {user?.username?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <div>
                      <div className="text-base font-medium text-gray-800">{user?.username || 'User'}</div>
                      <div className="text-sm font-medium text-gray-500">{user?.email || ''}</div>
                    </div>
                  </div>
                </div>
              ) : (
                <>
                  <Link
                    to="/signin"
                    className="block px-3 py-2 rounded-md text-base font-medium text-gray-900 hover:bg-gray-50"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Sign In
                  </Link>
                  <Link
                    to="/signup"
                    className="ml-4 inline-flex items-center px-4 py-2 border border-transparent text-base font-medium rounded-md text-white bg-rose-600 hover:bg-rose-700"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Get Started
                  </Link>
                </>
              )}
            </div>
            
            {isLoggedIn && (
              <div className="mt-3 px-2 space-y-1">
                {dropdownMenus.account.map((item, index) => (
                  item.action === 'logout' ? (
                    <button
                      key={index}
                      onClick={() => {
                        handleLogout();
                        setIsMenuOpen(false);
                      }}
                      className="block w-full text-left px-3 py-2 rounded-md text-base font-medium text-gray-900 hover:bg-gray-50"
                    >
                      {item.title}
                    </button>
                  ) : (
                    <Link
                      key={index}
                      to={item.href}
                      className="block px-3 py-2 rounded-md text-base font-medium text-gray-900 hover:bg-gray-50"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      {item.title}
                    </Link>
                  )
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  )
}

export default Header 