import { useDispatch, useSelector } from 'react-redux';
import { addSavedHome, removeSavedHome, saveHomeToServer, removeHomeFromServer } from '../redux/slices/savedHomesSlice';

const SavedHomesButton = ({ property, propertyId }) => {
  const dispatch = useDispatch();
  const savedHomes = useSelector(state => state.savedHomes.savedHomes);
  const { user } = useSelector(state => state.auth);
  
  // Handle either a property object or just a propertyId
  const id = propertyId || (property && property.id);
  
  // Check if this property is saved
  const isSaved = savedHomes.some(home => home.id === id);

  const handleToggleSave = () => {
    if (!user) {
      // Redirect to login or show login modal
      alert('Please log in to save properties');
      return;
    }

    if (isSaved) {
      // Remove from saved homes both locally and on server
      dispatch(removeSavedHome(id)); // Immediately update UI
      dispatch(removeHomeFromServer({ userId: user.id, propertyId: id }));
    } else {
      // If we have the full property object, use it
      if (property) {
        dispatch(addSavedHome(property)); // Immediately update UI
        dispatch(saveHomeToServer({ userId: user.id, propertyId: id }));
      } else {
        // If we only have the ID, we need to handle differently
        // Either fetch the property details first, or just save the ID
        dispatch(addSavedHome({ id })); // Save minimal property info
        dispatch(saveHomeToServer({ userId: user.id, propertyId: id }));
      }
    }
  };

  return (
    <button
      onClick={handleToggleSave}
      className={`p-2 rounded-full transition-colors duration-200 ${
        isSaved 
          ? 'bg-rose-100 text-rose-600 hover:bg-rose-200' 
          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
      }`}
      title={isSaved ? 'Xóa khỏi danh sách đã lưu' : 'Lưu vào danh sách'}
      aria-label={isSaved ? 'Xóa khỏi danh sách đã lưu' : 'Lưu vào danh sách'}
    >
      <svg 
        xmlns="http://www.w3.org/2000/svg" 
        className="h-5 w-5" 
        viewBox="0 0 20 20" 
        fill={isSaved ? 'currentColor' : 'none'}
        stroke="currentColor"
        strokeWidth="2"
      >
        <path 
          strokeLinecap="round" 
          strokeLinejoin="round" 
          d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" 
        />
      </svg>
    </button>
  );
};

export default SavedHomesButton; 