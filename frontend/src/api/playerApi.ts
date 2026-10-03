import { api } from "./axios";
import type {
    Player,
    CharacterSummary,
    PlayerCreationRequest,
    StatAllocationRequest,
} from "../types/player";
import type { AuthResponse } from "../types/auth";

export const playerApi = {
    create: async (request: PlayerCreationRequest): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>("/players", request);

        return response.data;
    },

    getMe: async (): Promise<Player> => {
        const response = await api.get<Player>("/players/me");

        return response.data;
    },

    getCharacters: async (): Promise<CharacterSummary[]> => {
        const response = await api.get<CharacterSummary[]>("/players");
        return response.data;
    },

    selectCharacter: async (playerId: number): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>(`/players/${playerId}/select`);
        return response.data;
    },

    allocateStats: async (
        request: StatAllocationRequest
    ): Promise<Player> => {
        const response = await api.post<Player>(
            "/players/stats/allocate",
            request
        );

        return response.data;
    },
};
