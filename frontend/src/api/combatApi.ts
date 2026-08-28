import { api } from "./axios";
import type {
    CombatReport,
    CombatTurnRequest,
} from "../types/combat";

export const combatApi = {
    executeTurn: async (
        request: CombatTurnRequest
    ): Promise<CombatReport> => {
        const response = await api.post<CombatReport>(
            "/combat/turn",
            request
        );

        return response.data;
    },
};
