import { Outlet } from 'react-router';
import NavbarAuth from './components/navbar/Navbar';
import Login from './pages/auth/Login';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import './utils/firebase.config';

function AppContent() {
  const { currentUser } = useAuth();

  if (!currentUser) {
    return <Login />;
  }

  return (
    <div className='d-flex'>
      <NavbarAuth />
      <main
        style={{
          marginLeft: '208px',
          marginRight: '208px',
          marginTop: '96px',
          marginBottom: '96px',
          width: '100%',
        }}
      >
        <Outlet />
      </main>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
