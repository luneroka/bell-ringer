import { Outlet } from 'react-router-dom';
import Navbar from '../components/navbar/Navbar';

function UnauthenticatedLayout() {
  return (
    <div className='d-flex'>
      <Navbar />
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

export default UnauthenticatedLayout;
