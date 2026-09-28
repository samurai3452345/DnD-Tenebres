import { useState } from 'react';
import { combatApi } from '../api/combatApi';
import type { CombatTurnRequest, CombatAction, CombatEvent } from '../types/combat';

export function useCombat() {
    const [loading, setLoading] = useState(false);
    const [events, setEvents] = useState<CombatEvent[]>([]);
    const [round, setRound] = useState(1);
    const [isPlayerDead, setIsPlayerDead] = useState(false);
    const [isEnemyDead, setIsEnemyDead] = useState(false);

    const executeTurn = async (monsterId: number, action: CombatAction, actionTargetId?: number) => {
        setLoading(true);
        try {
            const request: CombatTurnRequest = {
                action,
                targetId: monsterId,
                abilityId: action === 'CAST_SPELL' ? actionTargetId ?? null : null,
                itemId: action === 'USE_POTION' ? actionTargetId ?? null : null
            };

            const report = action === 'FLEE' ? await combatApi.flee() : await combatApi.executeTurn(request);

            setEvents(prev => [...prev, ...report.events]);
            setRound(report.round);
            setIsPlayerDead(report.status === 'DEFEAT');
            setIsEnemyDead(report.status === 'VICTORY' || report.status === 'FLED');

            return report;
        } catch (err: any) {
            console.error("Ошибка боя:", err);
            setEvents(prev => [...prev, { actor: 'Система', actionType: 'ERROR', target: '', value: 0, description: 'Ошибка сервера при выполнении хода' }]);
        } finally {
            setLoading(false);
        }
    };

    const resetCombat = () => {
        setEvents([]);
        setRound(1);
        setIsPlayerDead(false);
        setIsEnemyDead(false);
    };

    return { loading, events, round, isPlayerDead, isEnemyDead, executeTurn, resetCombat };
}
