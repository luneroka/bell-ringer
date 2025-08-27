import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage.jsx';
import QuizPage from './pages/QuizPage.jsx';
import QuizResults from './pages/QuizResults.jsx';
import { AuthProvider, useAuth } from './contexts/AuthContext.jsx';
import './utils/firebase.config';
import './styles/main.scss';
import Login from './pages/auth/Login.jsx';
import Register from './pages/auth/Register.jsx';
import Logout from './pages/auth/Logout.jsx';
import PrivateRoute from './contexts/PrivateRoute.jsx';
import AuthenticatedLayout from './layouts/AuthenticatedLayout';
import UnauthenticatedLayout from './layouts/UnauthenticatedLayout';
import HistoryPage from './pages/HistoryPage.jsx';

function ConditionalLayout() {
  const { currentUser } = useAuth();

  return currentUser ? <AuthenticatedLayout /> : <UnauthenticatedLayout />;
}

function AppRoutes() {
  return (
    <Routes>
      {/* Conditional home route */}
      <Route element={<ConditionalLayout />}>
        <Route path='/' element={<HomePage />} />
      </Route>

      {/* Unauthenticated Routes */}
      <Route element={<UnauthenticatedLayout />}>
        <Route path='/login' element={<Login />} />
        <Route path='/register' element={<Register />} />
        <Route path='/logout' element={<Logout />} />
      </Route>

      {/* Authenticated Routes */}
      <Route
        element={
          <PrivateRoute>
            <AuthenticatedLayout />
          </PrivateRoute>
        }
      >
        <Route path='/quiz' element={<QuizPage />} />
        <Route path='/quiz-results' element={<QuizResults />} />
        <Route path='/history' element={<HistoryPage />} />
      </Route>
    </Routes>
  );
}

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  </BrowserRouter>
);
