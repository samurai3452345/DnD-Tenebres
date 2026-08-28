import { api } from "./axios";
import type {
    Player,
    StatAllocationRequest,
} from "../types/player";

export const playerApi = {
    getMe: async (): Promise<Player> => {
        const response = await api.get<Player>("/players/me");

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
