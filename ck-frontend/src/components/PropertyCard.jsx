import { Link } from 'react-router-dom';
import SavedHomesButton from './SavedHomesButton';

const PropertyCard = ({ property }) => {
  const imageUrl = property.images?.[0] || 'https://via.placeholder.com/300x200';
  
  // Xác định loại tin đăng (Bán hoặc Cho thuê)
  const getListingType = () => {
    if (property.forSale) return { type: 'sale', label: 'BÁN' };
    if (property.forRent) return { type: 'rent', label: 'CHO THUÊ' };
    if (property.status === 'SOLD') return { type: 'sold', label: 'ĐÃ BÁN' };
    if (property.status === 'RENTED') return { type: 'rented', label: 'ĐÃ THUÊ' };
    return { type: 'unknown', label: 'KHÁC' };
  };
  
  const listingType = getListingType();
  
  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden group">
      {/* Property Image with tag for featured properties */}
      <div className="relative">
        <Link to={`/property/${property.id}`}>
          <img
            src={imageUrl}
            alt={property.title}
            className="h-64 w-full object-cover transform group-hover:scale-105 transition-transform duration-500"
          />
          
          {/* Listing type badge - positioned prominently */}
          <div className={`absolute top-0 left-0 ${
            listingType.type === 'sale' ? 'bg-red-600' : 
            listingType.type === 'rent' ? 'bg-green-600' :
            listingType.type === 'sold' ? 'bg-gray-600' :
            listingType.type === 'rented' ? 'bg-purple-600' :
            'bg-gray-600'
          } text-white px-4 py-1 text-sm font-bold`}>
            {listingType.label}
          </div>
        </Link>

        {property.status === 'featured' && (
          <div className="absolute top-3 right-12">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-100 text-rose-800">
              Nổi bật
            </span>
          </div>
        )}
        <div className="absolute top-2 right-2">
          <SavedHomesButton property={property} />
        </div>
      </div>

      <div className="p-4">
        <div>
          <h3 className="text-xl font-bold text-gray-900 mb-1">
            <Link to={`/property/${property.id}`} className="hover:text-rose-600 cursor-pointer">
              {property.title}
            </Link>
          </h3>
          <p className="text-gray-500 text-sm mb-2">{property.address}</p>
        </div>

        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mt-4 gap-2">
          <div className={`text-lg font-bold ${
            listingType.type === 'sale' || listingType.type === 'sold' ? 'text-red-600' : 
            listingType.type === 'rent' || listingType.type === 'rented' ? 'text-red-600' : 
            'text-rose-600'
          } whitespace-nowrap`}>
            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(property.price)}
            {(listingType.type === 'rent' || listingType.type === 'rented') && <span className="text-sm font-normal ml-1">/tháng</span>}
          </div>
          <div className="flex flex-wrap gap-3 text-sm text-gray-500">
            <span className="flex items-center whitespace-nowrap">
              {property.bedrooms} phòng ngủ
            </span>
            <span className="flex items-center whitespace-nowrap">
              {property.bathrooms} phòng tắm
            </span>
            <span className="flex items-center whitespace-nowrap">
              {property.sqft}m²
            </span>
          </div>
        </div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mt-3 border-t pt-3">
          <div className="flex items-center whitespace-nowrap text-sm">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800 capitalize">
              {property.type === 'house' ? 'Nhà riêng' : 
               property.type === 'apartment' ? 'Căn hộ' : 
               property.type === 'villa' ? 'Biệt thự' : 
               property.type === 'townhouse' ? 'Nhà phố' :
               property.type === 'commercial' ? 'Căn hộ kinh doanh' :
               property.type === 'office' ? 'Văn phòng' :
               property.type === 'warehouse' ? 'Kho hàng' :
               property.type === 'industrial' ? 'Nhà máy' :
               property.type === 'hotel' ? 'Khách sạn' :
               property.type === 'land' ? 'Đất' :
               property.type}
            </span>
          </div>
          <div className="flex items-center whitespace-nowrap text-sm">
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full capitalize
              ${property.status === 'available' ? 'bg-green-100 text-green-800' : 
                property.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                property.status === 'sold' ? 'bg-gray-100 text-gray-800' :
                property.status === 'published' ? 'bg-blue-100 text-blue-800' :
                'bg-rose-100 text-rose-800'}`}>
              {property.status === 'available' ? 'Còn trống' :
               property.status === 'PENDING' ? 'Đang chờ duyệt' :
               property.status === 'sold' ? 'Đã bán' :
               property.status === 'published' ? 'Đã xuất bản' :
               property.status}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PropertyCard; 