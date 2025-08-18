import { CiUser, CiSettings } from 'react-icons/ci';

function Navbar() {
  return (
    <header
      className='bg-primary position-sticky top-0 rounded-2'
      style={{
        width: '4rem',
        height: 'calc(100vh - 192px)',
        minHeight: '680px',
        marginTop: '96px',
        marginBottom: '96px',
        marginLeft: '32px',
        overflow: 'auto',
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
