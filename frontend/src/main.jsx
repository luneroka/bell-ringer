import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage.jsx';

// Import our custom CSS (which includes Bootstrap)
import './styles/main.scss';

// Import all of Bootstrap's JS
import * as bootstrap from 'bootstrap';

import App from './App.jsx';

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Routes>
      {/* HOME ROUTES */}
      <Route path='/' element={<App />}>
        <Route index element={<HomePage />} />
      </Route>
    </Routes>
  </BrowserRouter>
);
