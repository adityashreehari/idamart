import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite = the tool that runs the website while you develop (npm run dev)
// and bundles it for production (npm run build).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173, // must match app.cors.allowed-origins in the backend
  },
});
