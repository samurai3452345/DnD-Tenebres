import axios from "axios";

export const api = axios.create({
    baseURL: "/api/v1",
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use((config) => {
    const isPublicAuthRequest = config.url === "/auth/login" || config.url === "/auth/register";
    if (isPublicAuthRequest) {
        delete config.headers.Authorization;
        return config;
    }

    const token = localStorage.getItem("token") ?? sessionStorage.getItem("token");

    if (token && token !== "undefined") {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});
