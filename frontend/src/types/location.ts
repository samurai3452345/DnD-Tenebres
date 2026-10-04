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

export type LocationType = "SAFE_ZONE" | "NEUTRAL" | "DANGEROUS";

export type BiomeType = "FOREST" | "CAVE" | "RUINS" | "CITY" | "DUNGEON";

export interface LocationConnection {
    id: string;
    name: string;
    recommendedLevel: number;
    open: boolean;
    blockedReasons: string[];
}

export interface LocationResource {
    templateId: number;
    name: string;
    minAmount: number;
    maxAmount: number;
    findChance: number;
}

export interface Location {
    id: string;
    name: string;
    zoneName: string;
    description: string;
    type: LocationType;
    biome: BiomeType;
    recommendedLevel: number;
    effect: string;
    cleared: boolean;
    bossRoom: boolean;
    availableActions: string[];
    connections: LocationConnection[];
    resources: LocationResource[];
}
