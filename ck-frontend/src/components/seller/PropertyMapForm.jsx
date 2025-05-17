import { useState, useRef, useEffect } from 'react';
import { APIProvider, Map, AdvancedMarker, Pin, useMapsLibrary } from '@vis.gl/react-google-maps';

// Component tìm kiếm địa điểm riêng biệt
const PlaceAutocomplete = ({ onPlaceSelect }) => {
  const [autocomplete, setAutocomplete] = useState(null);
  const inputRef = useRef(null);
  const places = useMapsLibrary('places');

  useEffect(() => {
    if (!places || !inputRef.current) return;

    const options = {
      fields: ['geometry', 'name', 'formatted_address', 'address_components']
    };

    setAutocomplete(new places.Autocomplete(inputRef.current, options));
  }, [places]);

  useEffect(() => {
    if (!autocomplete) return;

    autocomplete.addListener('place_changed', () => {
      const place = autocomplete.getPlace();
      if (place) {
        onPlaceSelect(place);
      }
    });
  }, [autocomplete, onPlaceSelect]);

  return (
    <div className="w-full">
      <input
        ref={inputRef}
        type="text"
        placeholder="Tìm kiếm địa điểm..."
        className="w-full px-4 py-2 border-0 rounded-md focus:outline-none focus:ring-2 focus:ring-rose-500"
      />
    </div>
  );
};

const PropertyMapForm = ({ formData, setFormData }) => {
  const [mapsError, setMapsError] = useState(false);
  const markerRef = useRef(null);
  
  // API key - bạn sẽ thay thế bằng API key của mình
  const GOOGLE_MAPS_API_KEY = 'AIzaSyDLEtcyobN7FgOoRTQkMjRSltsmwEjkKf8-BIYhjYEY';
  
  // Handler for marker position change
  const handleMarkerPositionChange = (position) => {
    if (!position) return;
    
    setFormData(prev => ({
      ...prev,
      latitude: position.lat,
      longitude: position.lng
    }));
  };

  // Handler for map click
  const handleMapClick = (e) => {
    if (!e.latLng) return;
    
    const position = {
      lat: e.latLng.lat(),
      lng: e.latLng.lng()
    };
    
    setFormData(prev => ({
      ...prev,
      latitude: position.lat,
      longitude: position.lng
    }));
  };

  // Handler for place selection in autocomplete
  const handlePlaceSelect = (place) => {
    if (!place.geometry?.location) return;
    
    const location = place.geometry.location;
    const lat = location.lat();
    const lng = location.lng();
    
    // Extract address components
    let street = '';
    let city = '';
    let state = '';
    let zipCode = '';
    let district = '';
    let fullAddress = place.formatted_address || '';
    
    // Parse address components
    if (place.address_components) {
      place.address_components.forEach(component => {
        const types = component.types;
        
        if (types.includes('street_number') || types.includes('route')) {
          street = street ? `${street} ${component.long_name}` : component.long_name;
        }
        
        if (types.includes('locality')) {
          city = component.long_name;
        }
        
        if (types.includes('administrative_area_level_1')) {
          state = component.long_name;
        }
        
        if (types.includes('postal_code')) {
          zipCode = component.long_name;
        }
        
        if (types.includes('administrative_area_level_2') || types.includes('sublocality_level_1')) {
          district = component.long_name;
        }
      });
    }
    
    setFormData(prev => ({
      ...prev,
      latitude: lat,
      longitude: lng,
      address: fullAddress,
      street: street || prev.street,
      city: city || prev.city,
      state: state || prev.state,
      zipCode: zipCode || prev.zipCode,
      district: district || prev.district,
      neighborhoodName: district || city || prev.neighborhoodName
    }));
  };

  return (
    <div className="relative h-full w-full">
      {mapsError ? (
        // Hiển thị thông báo lỗi khi API không hoạt động
        <div className="h-full w-full bg-gray-100 flex flex-col items-center justify-center p-4">
          <div className="text-red-500 font-medium mb-2">
            Không thể tải bản đồ Google Maps
          </div>
          <p className="text-sm text-gray-600 text-center mb-4">
            Có vấn đề với API Google Maps. Vui lòng nhập tọa độ thủ công bên dưới.
          </p>
          <div className="flex space-x-2">
            <button
              type="button"
              onClick={() => setMapsError(false)}
              className="px-4 py-2 bg-blue-500 text-white rounded-md text-sm hover:bg-blue-600"
            >
              Thử lại
            </button>
          </div>
        </div>
      ) : (
        <>
          {/* Ô tìm kiếm địa điểm */}
          <div className="absolute top-2 left-0 right-0 z-10 mx-2">
            <div className="bg-white rounded-md shadow-md">
              <PlaceAutocomplete onPlaceSelect={handlePlaceSelect} />
            </div>
          </div>

          {/* Bản đồ Google */}
          <APIProvider apiKey={GOOGLE_MAPS_API_KEY} onError={() => setMapsError(true)}>
            <Map
              defaultCenter={{ lat: formData.latitude, lng: formData.longitude }}
              defaultZoom={14}
              gestureHandling="greedy"
              onClick={handleMapClick}
              mapId="ck-property-map"
              style={{ height: '100%', width: '100%' }}
            >
              <AdvancedMarker
                position={{ lat: formData.latitude, lng: formData.longitude }}
                draggable={true}
                ref={markerRef}
                onDragEnd={(e) => {
                  if (e.latLng) {
                    handleMarkerPositionChange({
                      lat: e.latLng.lat(),
                      lng: e.latLng.lng()
                    });
                  }
                }}
              >
                <Pin background={'#e53935'} glyphColor={'#fff'} borderColor={'#b71c1c'} />
              </AdvancedMarker>
            </Map>
          </APIProvider>
        </>
      )}
    </div>
  );
};

export default PropertyMapForm;
