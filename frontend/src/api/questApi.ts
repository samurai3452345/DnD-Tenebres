import { api } from "./axios";
import type {
    PlayerQuest,
    QuestTemplate,
} from "../types/quest";

export const questApi = {
    getAvailable: async (): Promise<QuestTemplate[]> => {
        const response = await api.get<QuestTemplate[]>(
            "/quests/available"
        );
        return response.data;
    },

    getActive: async (): Promise<PlayerQuest[]> => {
        const response = await api.get<PlayerQuest[]>(
            "/quests/active"
        );
        return response.data;
    },

    accept: async (
        questTemplateId: number
    ): Promise<PlayerQuest> => {
        const response = await api.post<PlayerQuest>(
            `/quests/accept/${questTemplateId}`
        );
        return response.data;
    },

    turnIn: async (
        playerQuestId: number
    ): Promise<void> => {
        await api.post(
            `/quests/turn-in/${playerQuestId}`
        );
    },
};