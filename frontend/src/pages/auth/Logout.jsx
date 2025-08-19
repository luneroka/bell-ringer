import { useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

function Logout() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    async function handleLogout() {
      await logout();
      navigate('/login');
    }
    handleLogout();
  }, [logout, navigate]);

  return <div>Logging out...</div>;
}

export default Logout;
