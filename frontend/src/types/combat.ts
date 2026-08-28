export type CombatAction =
    | "ATTACK"
    | "USE_POTION"
    | "CAST_SPELL"
    | "FLEE";

export interface CombatTurnRequest {
    monsterId: number;
    round: number;
    aliveEnemyCount: number;
    action: CombatAction;
    actionTargetName: string | null;
}

export interface CombatEvent {
    actor: string;
    actionType: string;
    target: string;
    value: number;
    description: string;
}

export interface CombatReport {
    round: number;
    events: CombatEvent[];
    isEnemyDead: boolean;
    isPlayerDead: boolean;
}
