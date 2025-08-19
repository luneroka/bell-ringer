import { useState } from 'react';
import { Link } from 'react-router-dom';
import { FaRegUser, FaUser } from 'react-icons/fa';
import { IoSettingsOutline, IoSettings } from 'react-icons/io5';
import { FaClockRotateLeft } from 'react-icons/fa6';
import { MdOutlineSpaceDashboard, MdSpaceDashboard } from 'react-icons/md';
import { RxCounterClockwiseClock } from 'react-icons/rx';

import { useAuth } from '../../contexts/AuthContext';

function NavbarAuth() {
  const [hoveredIcon, setHoveredIcon] = useState(null);
  const { currentUser } = useAuth();

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
          <Link to={currentUser ? '/' : '/login'}>
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

          <Link to={currentUser ? '/dashboard' : '/login'}>
            <div
              role='button'
              className='text-white'
              onMouseEnter={() => setHoveredIcon('dashboard')}
              onMouseLeave={() => setHoveredIcon(null)}
            >
              {hoveredIcon === 'dashboard' ? (
                <MdSpaceDashboard size={32} />
              ) : (
                <MdOutlineSpaceDashboard size={32} />
              )}
            </div>
          </Link>

          <Link to={currentUser ? '/history' : '/login'}>
            <div
              role='button'
              className='text-white'
              onMouseEnter={() => setHoveredIcon('history')}
              onMouseLeave={() => setHoveredIcon(null)}
            >
              {hoveredIcon === 'history' ? (
                <FaClockRotateLeft size={32} />
              ) : (
                <RxCounterClockwiseClock size={32} />
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

export default NavbarAuth;
