// Function to create Authorization header with JWT token if it exists
export default function authHeader() {
  const token = localStorage.getItem('token');
  
  if (token) {
    // Return authorization header with JWT token
    return { Authorization: `Bearer ${token}` };
  } else {
    // Return empty object if no token found
    return {};
  }
} 