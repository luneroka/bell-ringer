import { Outlet } from 'react-router-dom';
import NavbarAuth from '../components/navbar/NavbarAuth';

function AuthenticatedLayout() {
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

export default AuthenticatedLayout;
