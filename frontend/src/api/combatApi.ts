import { api } from "./axios";
import type { CombatActionRequest, CombatState } from "../types/combat";

export const combatApi = {
    current: async (): Promise<CombatState> => {
        const response = await api.get<CombatState>("/combat/current");
        return response.data;
    },

    executeAction: async (request: CombatActionRequest): Promise<CombatState> => {
        const response = await api.post<CombatState>("/combat/actions", request);
        return response.data;
    },
};
