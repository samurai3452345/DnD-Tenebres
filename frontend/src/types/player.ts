export interface PlayerStats {
    strength: number;
    dexterity: number;
    constitution: number;
    intelligence: number;
    wisdom: number;
    charisma: number;
}

export interface Player {
    playerId: number;
    playerName: string;
    level: number;
    experience: number;
    currentHp: number;
    maxHp: number;
    gold: number;
    stats: PlayerStats;
    statPoints: number;
    totalStrength: number;
    totalDexterity: number;
    totalConstitution: number;
    totalIntelligence: number;
    totalWisdom: number;
    totalCharisma: number;
    armorClass: number;
}

export interface StatAllocationRequest {
    addStrength: number;
    addDexterity: number;
    addConstitution: number;
    addIntelligence: number;
    addWisdom: number;
    addCharisma: number;
}
