export type CombatAction =
    | "ATTACK"
    | "USE_POTION"
    | "CAST_SPELL"
    | "FLEE";

export interface CombatTurnRequest {
    action: CombatAction;
    targetId: number | null;
    abilityId: number | null;
    itemId: number | null;
}

export interface CombatEvent {
    actor: string;
    actionType: string;
    target: string;
    value: number;
    description: string;
}

export interface CombatState {
    encounterId: number;
    status: "ACTIVE" | "VICTORY" | "DEFEAT" | "FLED" | "CANCELLED";
    round: number;
    player: { currentHp: number; maxHp: number; currentMp: number; maxMp: number };
    currentEnemy: { id: number; name: string; currentHp: number; maxHp: number } | null;
    remainingEnemies: number;
    events: CombatEvent[];
}
