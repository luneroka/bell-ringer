import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

function PrivateRoute({ children }) {
  const location = useLocation();
  const { currentUser } = useAuth();

  // Pass the attempted URL in state so that Login can redirect back after successful login
  return currentUser ? (
    children
  ) : (
    <Navigate to='/login' state={{ from: location.pathname }} />
  );
}

export default PrivateRoute;
