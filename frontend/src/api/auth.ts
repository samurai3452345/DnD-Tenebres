export interface LoginRequest {
    username: string;
    password: string;
}

export interface PlayerCreationRequest {
    name: string;
    strength: number;
    dexterity: number;
    constitution: number;
    intelligence: number;
    wisdom: number;
    charisma: number;
}

export interface RegisterRequest {
    username: string;
    password: string;
    playerRequest: PlayerCreationRequest;
}

export interface AuthResponse {
    token: string;
}