import { api } from "./axios";
import type {
    AuthResponse,
    LoginRequest,
    RegisterRequest,
} from "../types/auth";

export const authApi = {
    login: async (request: LoginRequest): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>(
            "/auth/login",
            request
        );

        return response.data;
    },

    register: async (
        request: RegisterRequest
    ): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>(
            "/auth/register",
            request
        );

        return response.data;
    },
};
