import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'node:path';

export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: path.resolve(__dirname, '../src/main/resources/static'),
    emptyOutDir: true,
    assetsDir: 'js',
    rollupOptions: {
      output: {
        entryFileNames: 'js/app.js',
        chunkFileNames: 'js/[name].js',
        assetFileNames: (assetInfo) => {
          if (assetInfo.name && assetInfo.name.endsWith('.css')) {
            return 'js/style.css';
          }
          return 'js/[name][extname]';
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  }
});
