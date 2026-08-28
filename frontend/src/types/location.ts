export type ExplorationEventType =
    | "MOVED"
    | "COMBAT_STARTED"
    | "FOUND_LOOT"
    | "NOTHING_FOUND"
    | "ERROR";

export interface ExplorationReport {
    eventType: ExplorationEventType;
    message: string;
    encounterMonsterId: number | null;
    foundItems: string[] | null;
}

export interface TravelRequest {
    targetLocationId: string;
}
