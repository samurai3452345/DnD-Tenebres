export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    hasCharacter: boolean;
    characterCount: number;
    selectedPlayerId: number | null;
}
