import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

// Port 5173 (dev) and 4173 (preview) are the origins the backend's CORS
// allow-list accepts outside production (backend/src/config/configuration.ts).
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
  preview: { port: 4173, strictPort: true },
  build: { sourcemap: false },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    restoreMocks: true,
    css: false,
  },
});
