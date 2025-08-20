import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const { login } = useAuth();

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setError('');
      setLoading(true);
      await login(email, password);
      navigate('/');
    } catch (error) {
      setError('Failed to log in: ' + error.message);
    }

    setLoading(false);
  }

  return (
    <div className='container mt-5'>
      <div className='row justify-content-center'>
        <div className='col-md-6'>
          <div className='card'>
            <div className='card-body'>
              <h2 className='card-title text-center mb-4'>Log In</h2>
              {error && <div className='alert alert-danger'>{error}</div>}
              <form onSubmit={handleSubmit}>
                <div className='mb-3'>
                  <label htmlFor='email' className='form-label'>
                    Email
                  </label>
                  <input
                    type='email'
                    className='form-control'
                    id='email'
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className='mb-3'>
                  <label htmlFor='password' className='form-label'>
                    Password
                  </label>
                  <input
                    type='password'
                    className='form-control'
                    id='password'
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
                <button
                  disabled={loading}
                  className='btn btn-primary w-100'
                  type='submit'
                >
                  {loading ? 'Logging in...' : 'Log In'}
                </button>
              </form>

              <div className='mt-5'>
                Need an account? <Link to='/register'>Sign Up</Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
