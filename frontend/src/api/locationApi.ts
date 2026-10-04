import { api } from "./axios";
import type { Location } from "../types/location";

export const locationApi = {
    getCurrent: async (): Promise<Location> => {
        const response = await api.get<Location>("/world/current-location");
        return response.data;
    },
};
