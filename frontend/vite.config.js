import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In dev mode, forward /api calls to the services running via docker compose.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api/auth": { target: "http://localhost:8084", rewrite: (p) => p.replace(/^\/api/, "") },
      "/api/notes": { target: "http://localhost:8081", rewrite: (p) => p.replace(/^\/api/, "") },
      "/api/tasks": { target: "http://localhost:8082", rewrite: (p) => p.replace(/^\/api/, "") },
    },
  },
});
