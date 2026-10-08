import React from 'react';
import type { CombatActionRequest, CombatState } from '../../types/combat';
import CombatLog from './CombatLog';
import CombatActions from './CombatActions';

interface CombatPanelProps {
    state: CombatState;
    monsterName: string;
    isLoading: boolean;
    onAction: (request: CombatActionRequest) => void;
}

const statusLabels: Partial<Record<CombatState["status"], string>> = {
    VICTORY: "ПОБЕДА!",
    DEFEAT: "ВЫ ПОГИБЛИ",
    FLED: "ПОБЕГ УДАЛСЯ",
    CANCELLED: "БОЙ ПРЕРВАН",
};

export default function CombatPanel({ state, monsterName, isLoading, onAction }: CombatPanelProps) {
    const isFinished = state.status !== "ACTIVE";
    const enemy = state.currentEnemy;

    return (
        <div style={{ border: '2px solid #e74c3c', borderRadius: '8px', overflow: 'hidden', backgroundColor: '#fff', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>

            <div style={{ backgroundColor: '#e74c3c', color: 'white', padding: '12px 15px', fontWeight: 'bold', fontSize: '1.2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>⚔️ Противник: {monsterName}</span>
                {statusLabels[state.status] && (
                    <span style={{ color: '#f1c40f', background: 'rgba(0,0,0,0.2)', padding: '2px 8px', borderRadius: '4px' }}>
                        {statusLabels[state.status]}
                    </span>
                )}
            </div>

            <div style={{ padding: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: '12px', marginBottom: '12px', color: '#2c1810' }}>
                    <span>
                        <strong>{state.player.name}</strong>: {state.player.currentHp}/{state.player.maxHp} HP · {state.player.currentMp}/{state.player.maxMp} MP
                    </span>
                    <span>
                        Раунд {state.round}
                        {enemy ? ` · ${enemy.currentHp}/${enemy.maxHp} HP` : ''}
                        {state.remainingEnemies > 1 ? ` · врагов: ${state.remainingEnemies}` : ''}
                    </span>
                </div>
                <CombatLog events={state.journal} />

                <div style={{ marginTop: '15px' }}>
                    <CombatActions
                        allowedActions={state.allowedActions}
                        abilities={state.availableAbilities}
                        potions={state.availablePotions}
                        currentEnemyId={enemy?.id ?? null}
                        onAction={onAction}
                        disabled={isFinished || isLoading}
                    />
                </div>
            </div>
        </div>
    );
}
