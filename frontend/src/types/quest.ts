export type QuestType =
    | "KILL_MONSTERS"
    | "GATHER_ITEMS"
    | "CLEAR_LOCATION";

export type QuestStatus =
    | "ACTIVE"
    | "COMPLETED"
    | "REWARDED";

export interface QuestTemplate {
    id: number;
    name: string;
    description: string;
    questType: QuestType;
    targetIdentifier: string;
    targetCount: number;
    rewardXp: number;
    rewardGold: number;
}

export interface PlayerQuest {
    id: number;
    currentProgress: number;
    questStatus: QuestStatus;
    questTemplate: QuestTemplate;
}
