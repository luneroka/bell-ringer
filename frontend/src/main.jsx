import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage.jsx';
import App from './App.jsx';
import QuizPage from './pages/QuizPage.jsx';
import { AuthProvider } from './contexts/AuthContext.jsx';
import './utils/firebase.config';
import './styles/main.scss';
import Login from './pages/auth/Login.jsx';
import Register from './pages/auth/Register.jsx';
import Logout from './pages/auth/Logout.jsx'; // Adjust the path if needed

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AuthProvider>
      <Routes>
        {/* HOME ROUTES */}
        <Route path='/' element={<App />}>
          <Route index element={<HomePage />} />
          <Route path='/quiz' element={<QuizPage />} />
        </Route>

        <Route path='/login' element={<Login />} />
        <Route path='/register' element={<Register />} />
        <Route path='/logout' element={<Logout />} />
      </Routes>
    </AuthProvider>
  </BrowserRouter>
);
