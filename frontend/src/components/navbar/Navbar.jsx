import { CiUser, CiSettings } from 'react-icons/ci';

function Navbar() {
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
        <CiUser size={32} className='text-white' />
        <CiSettings size={32} className='text-white' />
      </nav>
    </header>
  );
}

export default Navbar;
