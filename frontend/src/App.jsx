import { Outlet } from 'react-router';
import NavbarAuth from './components/navbar/Navbar';

function App() {
  return (
    <div className='d-flex'>
      <NavbarAuth />
      <main>
        <Outlet />
      </main>
    </div>
  );
}

export default App;
