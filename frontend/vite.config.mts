import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

console.log("🔥 VITE CONFIG LOADED");

export default defineConfig({
    plugins: [react()],

    server: {
        host: "127.0.0.1",
        port: 5173,
        strictPort: true,
        proxy: {
            "/api": {
                target: "http://127.0.0.1:8081",
                changeOrigin: true,
            },
        },
    },
});
