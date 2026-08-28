import { api } from "./axios";
import type { RestReport } from "../types/rest";

export const restApi = {

    shortRest: async (): Promise<RestReport> => {
        const response = await api.post<RestReport>(
            "/rest/short"
        );

        return response.data;
    },

    longRest: async (): Promise<RestReport> => {
        const response = await api.post<RestReport>(
            "/rest/long"
        );

        return response.data;
    },

};
