import { useCallback, useState } from "react";

import { combatApi } from "../api/combatApi";
import type { CombatActionRequest, CombatState } from "../types/combat";

export function useCombat() {
    const [loading, setLoading] = useState(false);
    const [state, setState] = useState<CombatState | null>(null);
    const [lastEnemyName, setLastEnemyName] = useState<string | null>(null);

    const rememberState = useCallback((nextState: CombatState) => {
        setState(nextState);
        if (nextState.currentEnemy?.name) {
            setLastEnemyName(nextState.currentEnemy.name);
        }
        return nextState;
    }, []);

    const loadCurrent = useCallback(async () => {
        setLoading(true);
        try {
            return rememberState(await combatApi.current());
        } finally {
            setLoading(false);
        }
    }, [rememberState]);

    const executeAction = useCallback(async (request: CombatActionRequest) => {
        setLoading(true);
        try {
            return rememberState(await combatApi.executeAction(request));
        } finally {
            setLoading(false);
        }
    }, [rememberState]);

    const adoptState = useCallback((nextState: CombatState) => {
        return rememberState(nextState);
    }, [rememberState]);

    const resetCombat = useCallback(() => {
        setState(null);
        setLastEnemyName(null);
    }, []);

    return {
        loading,
        state,
        enemyName: state?.currentEnemy?.name ?? lastEnemyName ?? "Неизвестный противник",
        loadCurrent,
        executeAction,
        adoptState,
        resetCombat,
    };
}
