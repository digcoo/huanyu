import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    port: 5174,
    proxy: {
      '/tts': {
        target: 'http://localhost:9010',
        changeOrigin: true
      }
    }
  }
});
