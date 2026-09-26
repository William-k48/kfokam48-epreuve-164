import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 4200,
    strictPort: true,
    watch: {
      usePolling: true,
    },
    // Issue #35 : en dev, /api est relayé vers le backend pour rester sur une
    // seule origine. En production, c'est Nginx qui assure ce relais.
    proxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
    },
  },
  preview: {
    host: '0.0.0.0',
    port: 4200,
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
    // Pool 'threads' : les workers 'forks' ne démarrent pas de manière fiable
    // sur ce poste Windows (timeout au lancement du worker).
    pool: 'threads',
  },
})