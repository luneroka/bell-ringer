import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage.jsx';
import App from './App.jsx';
import QuizPage from './pages/QuizPage.jsx';
import { AuthProvider } from './contexts/AuthContext.jsx';
import './styles/main.scss';
import Login from './components/auth/Login.jsx';

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AuthProvider>
      <Routes>
        {/* HOME ROUTES */}
        <Route path='/' element={<App />}>
          <Route index element={<HomePage />} />
        </Route>

        <Route path='/login' element={<Login />} />
      </Routes>
    </AuthProvider>
  </BrowserRouter>
);
