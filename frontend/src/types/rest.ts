import type { CombatState } from "./combat";

export interface RestReport {
    message: string;
    isAmbushed: boolean;
    locationId: string | null;
    encounter: CombatState | null;
}
