import { useState } from 'react';
import { Link } from 'react-router-dom';
import { FaRegUser, FaUser } from 'react-icons/fa';
import {
  IoHomeOutline,
  IoHome,
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
              onMouseEnter={() => setHoveredIcon('hom')}
              onMouseLeave={() => setHoveredIcon(null)}
            >
              {hoveredIcon === 'hom' ? (
                <IoHome size={32} />
              ) : (
                <IoHomeOutline size={32} />
              )}
            </div>
          </Link>
          <Link to='/login'>
            <div
              role='button'
              className='text-white'
              onMouseEnter={() => setHoveredIcon('user')}
              onMouseLeave={() => setHoveredIcon(null)}
            >
              {hoveredIcon === 'user' ? (
                <FaUser size={32} />
              ) : (
                <FaRegUser size={32} />
              )}
            </div>
          </Link>
        </div>

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
      </nav>
    </header>
  );
}

export default Navbar;
