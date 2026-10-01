import { useState } from 'react';
import { combatApi } from '../api/combatApi';
import type { CombatTurnRequest, CombatAction, CombatEvent } from '../types/combat';

export function useCombat() {
    const [loading, setLoading] = useState(false);
    const [events, setEvents] = useState<CombatEvent[]>([]);
    const [round, setRound] = useState(1);
    const [isPlayerDead, setIsPlayerDead] = useState(false);
    const [isEnemyDead, setIsEnemyDead] = useState(false);

    const executeTurn = async (monsterId: number, action: CombatAction, actionTargetName?: string) => {
        setLoading(true);
        try {
            const request: CombatTurnRequest = {
                monsterId,
                round,
                aliveEnemyCount: 1, // Для MVP считаем, что враг один
                action,
                actionTargetName: actionTargetName || null
            };

            const report = await combatApi.executeTurn(request);

            setEvents(prev => [...prev, ...report.events]);
            setRound(report.round + 1);
            setIsPlayerDead(report.isPlayerDead);
            setIsEnemyDead(report.isEnemyDead);

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