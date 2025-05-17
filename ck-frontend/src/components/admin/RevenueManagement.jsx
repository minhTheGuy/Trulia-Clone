import { useState, useEffect } from 'react';

const RevenueManagement = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeRange, setTimeRange] = useState('month');
  const [yearFilter, setYearFilter] = useState(new Date().getFullYear());
  const [monthFilter, setMonthFilter] = useState(new Date().getMonth() + 1);
  const [quarterFilter, setQuarterFilter] = useState(Math.floor((new Date().getMonth() / 3) + 1));

  // Mock data
  const mockRevenueData = {
    summary: {
      totalRevenue: 2580000000,
      comparedToPrevious: 15, // % increase from previous period
      transactions: 126,
      avgTransactionValue: 20476190,
      pendingPayments: 3,
      pendingValue: 75000000
    },
    bySource: [
      { source: 'Tin rao vặt cao cấp', amount: 850000000, percentage: 32.9 },
      { source: 'Tin đăng VIP', amount: 980000000, percentage: 38.0 },
      { source: 'Dịch vụ môi giới', amount: 520000000, percentage: 20.2 },
      { source: 'Quảng cáo', amount: 230000000, percentage: 8.9 }
    ],
    // Monthly revenue data for charts
    monthlyData: [
      { month: 'T1', revenue: 190000000 },
      { month: 'T2', revenue: 210000000 },
      { month: 'T3', revenue: 175000000 },
      { month: 'T4', revenue: 220000000 },
      { month: 'T5', revenue: 240000000 },
      { month: 'T6', revenue: 195000000 },
      { month: 'T7', revenue: 260000000 },
      { month: 'T8', revenue: 235000000 },
      { month: 'T9', revenue: 285000000 },
      { month: 'T10', revenue: 310000000 },
      { month: 'T11', revenue: 260000000 },
      { month: 'T12', revenue: 0 }
    ],
    // Recent transactions
    recentTransactions: [
      { id: 1, date: '2023-11-15', user: 'truongnguyen', type: 'Tin VIP', amount: 15000000, status: 'completed' },
      { id: 2, date: '2023-11-14', user: 'anhtuan', type: 'Quảng cáo', amount: 5000000, status: 'completed' },
      { id: 3, date: '2023-11-13', user: 'huongtran', type: 'Tin cao cấp', amount: 8000000, status: 'completed' },
      { id: 4, date: '2023-11-12', user: 'thanhle', type: 'Dịch vụ môi giới', amount: 25000000, status: 'pending' },
      { id: 5, date: '2023-11-10', user: 'minhvu', type: 'Tin VIP', amount: 15000000, status: 'completed' }
    ]
  };

  // Format currency values
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
  };

  useEffect(() => {
    // Simulating API fetch
    const fetchData = async () => {
      try {
        // Would be replaced with actual API call
        // const data = await adminService.getRevenueData(timeRange, yearFilter, monthFilter, quarterFilter);
        
        // For now, using mock data
        setTimeout(() => {
          setLoading(false);
        }, 1000);
      } catch (err) {
        setError('Có lỗi xảy ra khi tải dữ liệu doanh thu');
        setLoading(false);
      }
    };
    
    setLoading(true);
    fetchData();
  }, [timeRange, yearFilter, monthFilter, quarterFilter]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border-l-4 border-red-400 p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center space-y-4 sm:space-y-0">
        <h3 className="text-lg leading-6 font-medium text-gray-900">Thống kê doanh thu</h3>
        <div className="flex space-x-4">
          <select
            value={timeRange}
            onChange={(e) => setTimeRange(e.target.value)}
            className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block sm:text-sm border-gray-300 rounded-md"
          >
            <option value="month">Theo tháng</option>
            <option value="quarter">Theo quý</option>
            <option value="year">Theo năm</option>
          </select>
          
          {timeRange === 'month' && (
            <>
              <select
                value={monthFilter}
                onChange={(e) => setMonthFilter(parseInt(e.target.value))}
                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block sm:text-sm border-gray-300 rounded-md"
              >
                {Array.from({ length: 12 }, (_, i) => i + 1).map(month => (
                  <option key={month} value={month}>Tháng {month}</option>
                ))}
              </select>
              <select
                value={yearFilter}
                onChange={(e) => setYearFilter(parseInt(e.target.value))}
                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block sm:text-sm border-gray-300 rounded-md"
              >
                {Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i).map(year => (
                  <option key={year} value={year}>{year}</option>
                ))}
              </select>
            </>
          )}
          
          {timeRange === 'quarter' && (
            <>
              <select
                value={quarterFilter}
                onChange={(e) => setQuarterFilter(parseInt(e.target.value))}
                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block sm:text-sm border-gray-300 rounded-md"
              >
                {[1, 2, 3, 4].map(quarter => (
                  <option key={quarter} value={quarter}>Quý {quarter}</option>
                ))}
              </select>
              <select
                value={yearFilter}
                onChange={(e) => setYearFilter(parseInt(e.target.value))}
                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block sm:text-sm border-gray-300 rounded-md"
              >
                {Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i).map(year => (
                  <option key={year} value={year}>{year}</option>
                ))}
              </select>
            </>
          )}
          
          {timeRange === 'year' && (
            <select
              value={yearFilter}
              onChange={(e) => setYearFilter(parseInt(e.target.value))}
              className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block sm:text-sm border-gray-300 rounded-md"
            >
              {Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i).map(year => (
                <option key={year} value={year}>{year}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {/* Tổng quan doanh thu */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-blue-500 rounded-md p-3">
                <svg className="h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Tổng doanh thu</dt>
                  <dd className="flex items-baseline">
                    <div className="text-2xl font-semibold text-gray-900">
                      {formatCurrency(mockRevenueData.summary.totalRevenue)}
                    </div>
                    {mockRevenueData.summary.comparedToPrevious > 0 ? (
                      <div className="ml-2 flex items-baseline text-sm font-semibold text-green-600">
                        <svg className="self-center flex-shrink-0 h-5 w-5 text-green-500" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                          <path fillRule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L11 7.414V15a1 1 0 11-2 0V7.414L6.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
                        </svg>
                        <span className="sr-only">Tăng</span>
                        {mockRevenueData.summary.comparedToPrevious}%
                      </div>
                    ) : (
                      <div className="ml-2 flex items-baseline text-sm font-semibold text-red-600">
                        <svg className="self-center flex-shrink-0 h-5 w-5 text-red-500" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                          <path fillRule="evenodd" d="M14.707 10.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L9 12.586V5a1 1 0 012 0v7.586l2.293-2.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                        <span className="sr-only">Giảm</span>
                        {Math.abs(mockRevenueData.summary.comparedToPrevious)}%
                      </div>
                    )}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-green-500 rounded-md p-3">
                <svg className="h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Giao dịch thành công</dt>
                  <dd className="text-2xl font-semibold text-gray-900">{mockRevenueData.summary.transactions}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-indigo-500 rounded-md p-3">
                <svg className="h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Giá trị trung bình</dt>
                  <dd className="text-2xl font-semibold text-gray-900">{formatCurrency(mockRevenueData.summary.avgTransactionValue)}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-yellow-500 rounded-md p-3">
                <svg className="h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Chờ thanh toán</dt>
                  <dd className="text-2xl font-semibold text-gray-900">
                    {mockRevenueData.summary.pendingPayments} ({formatCurrency(mockRevenueData.summary.pendingValue)})
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Phân tích theo nguồn thu */}
      <div className="bg-white shadow overflow-hidden sm:rounded-lg">
        <div className="px-4 py-5 sm:px-6">
          <h3 className="text-lg leading-6 font-medium text-gray-900">Doanh thu theo nguồn</h3>
        </div>
        <div className="border-t border-gray-200">
          <div className="px-4 py-5 sm:p-6">
            <div className="space-y-4">
              {mockRevenueData.bySource.map((item, index) => (
                <div key={index}>
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium text-gray-500">{item.source}</span>
                    <span className="text-sm font-medium text-gray-900">{formatCurrency(item.amount)} ({item.percentage}%)</span>
                  </div>
                  <div className="mt-1 relative">
                    <div className="overflow-hidden h-2 text-xs flex rounded bg-gray-200">
                      <div 
                        style={{ width: `${item.percentage}%` }} 
                        className={`shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center ${
                          index % 4 === 0 ? 'bg-blue-500' : 
                          index % 4 === 1 ? 'bg-green-500' : 
                          index % 4 === 2 ? 'bg-indigo-500' : 
                          'bg-yellow-500'
                        }`}
                      ></div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Danh sách giao dịch gần đây */}
      <div className="bg-white shadow overflow-hidden sm:rounded-lg">
        <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
          <h3 className="text-lg leading-6 font-medium text-gray-900">Giao dịch gần đây</h3>
          <button
            type="button"
            className="inline-flex items-center px-3 py-1 border border-transparent text-sm font-medium rounded text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Xem tất cả
          </button>
        </div>
        <div className="border-t border-gray-200">
          <div className="flow-root">
            <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
                <table className="min-w-full divide-y divide-gray-300">
                  <thead>
                    <tr>
                      <th scope="col" className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-0">ID</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Ngày</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Người dùng</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Loại</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Giá trị</th>
                      <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white">
                    {mockRevenueData.recentTransactions.map((transaction) => (
                      <tr key={transaction.id}>
                        <td className="whitespace-nowrap py-4 pl-4 pr-3 text-sm sm:pl-0">
                          <div className="font-medium text-gray-900">#{transaction.id}</div>
                        </td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{transaction.date}</td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{transaction.user}</td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{transaction.type}</td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-900 font-medium">{formatCurrency(transaction.amount)}</td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm">
                          {transaction.status === 'completed' ? (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              Hoàn thành
                            </span>
                          ) : (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                              Chờ xử lý
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Phần hiển thị biểu đồ có thể được thêm vào sau với thư viện như Chart.js, Recharts, etc. */}
      <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-yellow-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-yellow-700">
              Biểu đồ trực quan sẽ được triển khai với thư viện Chart.js hoặc Recharts để hiển thị dữ liệu theo thời gian.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RevenueManagement; 