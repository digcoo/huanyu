import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    host: '127.0.0.1',
    port: 5174,
    proxy: {
      '/tts': {
        target: 'http://localhost:9010',
        changeOrigin: true
      }
    }
  }
});
