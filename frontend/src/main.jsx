import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage.jsx';
import App from './App.jsx';
import QuizPage from './pages/QuizPage.jsx';

// Import our custom CSS (which includes Bootstrap)
import './styles/main.scss';

// Import all of Bootstrap's JS
import * as bootstrap from 'bootstrap';

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Routes>
      {/* HOME ROUTES */}
      <Route path='/' element={<App />}>
        <Route index element={<HomePage />} />
        <Route path='/quiz' element={<QuizPage />} />
      </Route>
    </Routes>
  </BrowserRouter>
);
