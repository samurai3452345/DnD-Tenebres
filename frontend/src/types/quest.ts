export type QuestType =
    | "KILL_MONSTERS"
    | "GATHER_ITEMS"
    | "CLEAR_LOCATION"
    | "VISIT_LOCATION"
    | "USE_ITEM"
    | "DEFEAT_BOSS"
    | "TALK_TO_NPC"
    | "TRADE"
    | "REACH_LEVEL"
    | "USE_ABILITY";

export type QuestSource = "MAIN_STORY" | "NPC" | "GUILD";

export type QuestStatus =
    | "ACTIVE"
    | "COMPLETED"
    | "REWARDED";

export interface QuestTemplate {
    id: number;
    name: string;
    description: string;
    type: QuestType;
    source: QuestSource;
    targetIdentifier: string;
    targetCount: number;
    rewardXp: number;
    rewardGold: number;
    minLevel: number;
    acceptLocationId: string | null;
    turnInLocationId: string | null;
    prerequisiteQuestId: number | null;
    repeatable: boolean;
    rewardItemTemplateId: number | null;
    rewardItemAmount: number;
}

export interface PlayerQuest {
    id: number;
    currentProgress: number;
    targetCount: number;
    status: QuestStatus;
    canTurnIn: boolean;
    quest: QuestTemplate;
}
