import { Outlet } from 'react-router';
import NavbarAuth from './components/navbar/Navbar';
import Login from './components/auth/Login';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import './utils/firebase.config';

function App() {
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

export default App;
