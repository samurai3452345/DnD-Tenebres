import { api } from "./axios";
import type {
    ExplorationReport,
    TravelRequest,
} from "../types/location";

export const explorationApi = {
    hunt: async (): Promise<ExplorationReport> => {
        const response = await api.post<ExplorationReport>(
            "/exploration/hunt"
        );

        return response.data;
    },

    search: async (): Promise<ExplorationReport> => {
        const response = await api.post<ExplorationReport>(
            "/exploration/search"
        );

        return response.data;
    },

    travel: async (
        request: TravelRequest
    ): Promise<ExplorationReport> => {
        const response = await api.post<ExplorationReport>(
            "/exploration/travel",
            request
        );

        return response.data;
    },
};
