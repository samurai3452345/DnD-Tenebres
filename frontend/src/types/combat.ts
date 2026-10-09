export type CombatAction =
    | "ATTACK"
    | "USE_POTION"
    | "CAST_SPELL"
    | "FLEE";

export type EncounterStatus =
    | "ACTIVE"
    | "VICTORY"
    | "DEFEAT"
    | "FLED"
    | "CANCELLED";

export type EncounterReason =
    | "HUNT"
    | "TRAVEL"
    | "REST_AMBUSH"
    | "SCRIPTED";

export interface CombatActionRequest {
    action: CombatAction;
    targetId: number | null;
    abilityId: number | null;
    itemId: number | null;
}

export interface CombatEvent {
    round?: number | null;
    actor: string;
    actionType: string;
    target: string;
    value: number;
    description: string;
}

export interface CombatEffect {
    type: string;
    remainingRounds: number;
    power: number;
    category: string;
}

export interface CombatPlayerState {
    id: number;
    name: string;
    level: number;
    currentHp: number;
    maxHp: number;
    currentMp: number;
    maxMp: number;
    avatarKey: string;
    effects: CombatEffect[];
}

export interface CombatEnemyState {
    id: number;
    name: string;
    level: number;
    currentHp: number;
    maxHp: number;
    elements: string[];
    avatarKey: string;
    effects: CombatEffect[];
}

export interface CombatAbilityState {
    id: number;
    name: string;
    tier: number;
    manaCost: number;
    element: string;
    available: boolean;
    unavailableReason: string | null;
}

export interface CombatPotionState {
    itemId: number;
    templateId: number;
    name: string;
    amount: number;
    action: string;
    available: boolean;
}

export interface CombatState {
    encounterId: number;
    status: EncounterStatus;
    reason: EncounterReason;
    round: number;
    player: CombatPlayerState;
    currentEnemy: CombatEnemyState | null;
    remainingEnemies: number;
    allowedActions: CombatAction[];
    availableAbilities: CombatAbilityState[];
    availablePotions: CombatPotionState[];
    events: CombatEvent[];
    journal: CombatEvent[];
    startedAt: string;
}
