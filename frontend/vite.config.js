import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3001,
    proxy: {
      // '/api'
      '/api': {  // Proxy all requests starting with /api
        target: 'http://localhost:8081', // Your backend server
        changeOrigin: true,
        secure: false, // If using HTTPS with self-signed certs, set to false
        rewrite: (path) => path.replace(/^\/api/, '') // Optional: Remove '/api' prefix
      }
    }
  }
})
