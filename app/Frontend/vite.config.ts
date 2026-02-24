import path from "path"
import tailwindcss from "@tailwindcss/vite"
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 3000,
    strictPort: true
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  define: {
    'process.env.VITE_API_AUTH_URL': JSON.stringify(process.env.VITE_API_AUTH_URL),
    'process.env.VITE_API_TRANSACTIONS_URL': JSON.stringify(process.env.VITE_API_TRANSACTIONS_URL),
    'process.env.VITE_API_REPORTS_URL': JSON.stringify(process.env.VITE_API_REPORTS_URL),
  }
})
