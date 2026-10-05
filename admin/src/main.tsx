import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { App } from './app/App';
import { createApiClient } from './core/api/apiClient';
import { createMemoryTokenStore } from './core/api/tokenStore';
import { API_BASE_URL, API_PREFIX } from './core/config/env';
import './styles/tokens.css';
import './styles/base.css';
import './styles/components.css';
import './styles/layout.css';

const api = createApiClient({
  baseUrl: `${API_BASE_URL}${API_PREFIX}`,
  tokenStore: createMemoryTokenStore(),
});

const container = document.getElementById('root');
if (!container) {
  throw new Error('Root element #root is missing from index.html.');
}

createRoot(container).render(
  <StrictMode>
    <BrowserRouter>
      <App api={api} />
    </BrowserRouter>
  </StrictMode>,
);
