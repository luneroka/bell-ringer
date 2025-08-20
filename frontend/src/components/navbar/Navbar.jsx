import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  IoHome,
  IoHomeOutline,
  IoLogInOutline,
  IoLogIn,
  IoPersonAddOutline,
  IoPersonAdd,
  IoSettingsOutline,
  IoSettings,
} from 'react-icons/io5';

function Navbar() {
  const [hoveredIcon, setHoveredIcon] = useState(null);

  return (
    <header
      className='bg-primary rounded-2'
      style={{
        position: 'fixed',
        left: '32px',
        top: '96px',
        width: '4rem',
        height: 'calc(100vh - 192px)',
        minHeight: '480px',
        overflow: 'auto',
        zIndex: 1030,
      }}
    >
      <nav
        className='d-flex flex-column align-items-center justify-content-between py-5'
        style={{ height: '100%' }}
      >
        <div className='d-flex flex-column gap-4'>
          <Link to='/'>
            <div
              role='button'
              className='text-white'
              onMouseEnter={() => setHoveredIcon('home')}
              onMouseLeave={() => setHoveredIcon(null)}
            >
              {hoveredIcon === 'home' ? (
                <IoHome size={32} />
              ) : (
                <IoHomeOutline size={32} />
              )}
            </div>
          </Link>
        </div>

        <div className='d-flex flex-column gap-4'>
          <Link to='/login'>
            <div
              role='button'
              className='text-white'
              onMouseEnter={() => setHoveredIcon('login')}
              onMouseLeave={() => setHoveredIcon(null)}
            >
              {hoveredIcon === 'login' ? (
                <IoLogIn size={32} />
              ) : (
                <IoLogInOutline size={32} />
              )}
            </div>
          </Link>
          <Link to='/register'>
            <div
              role='button'
              className='text-white'
              onMouseEnter={() => setHoveredIcon('register')}
              onMouseLeave={() => setHoveredIcon(null)}
            >
              {hoveredIcon === 'register' ? (
                <IoPersonAdd size={32} />
              ) : (
                <IoPersonAddOutline size={32} />
              )}
            </div>
          </Link>
          <div
            role='button'
            className='text-white'
            onMouseEnter={() => setHoveredIcon('settings')}
            onMouseLeave={() => setHoveredIcon(null)}
          >
            {hoveredIcon === 'settings' ? (
              <IoSettings size={32} />
            ) : (
              <IoSettingsOutline size={32} />
            )}
          </div>
        </div>
      </nav>
    </header>
  );
}

export default Navbar;
