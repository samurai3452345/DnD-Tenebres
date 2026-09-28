import { api } from "./axios";
import type {
    CombatState,
    CombatTurnRequest,
} from "../types/combat";

export const combatApi = {
    executeTurn: async (
        request: CombatTurnRequest
    ): Promise<CombatState> => {
        const response = await api.post<CombatState>(
            "/combat/actions",
            request
        );

        return response.data;
    },
    current: async (): Promise<CombatState> => (await api.get<CombatState>("/combat/current")).data,
    flee: async (): Promise<CombatState> => (await api.post<CombatState>("/combat/flee")).data,
};
