import React from 'react';
import type { CombatEvent, CombatAction } from '../../types/combat';
import CombatLog from './CombatLog';
import CombatActions from './CombatActions';

interface CombatPanelProps {
    monsterName: string;
    events: CombatEvent[];
    isPlayerDead: boolean;
    isEnemyDead: boolean;
    isLoading: boolean;
    onAction: (action: CombatAction, targetName?: string) => void;
}

export default function CombatPanel({ monsterName, events, isPlayerDead, isEnemyDead, isLoading, onAction }: CombatPanelProps) {
    return (
        <div style={{ border: '2px solid #e74c3c', borderRadius: '8px', overflow: 'hidden', backgroundColor: '#fff', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>

            <div style={{ backgroundColor: '#e74c3c', color: 'white', padding: '12px 15px', fontWeight: 'bold', fontSize: '1.2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>⚔️ Противник: {monsterName}</span>
                {isEnemyDead && <span style={{ color: '#f1c40f', background: 'rgba(0,0,0,0.2)', padding: '2px 8px', borderRadius: '4px' }}>ПОБЕДА!</span>}
                {isPlayerDead && <span style={{ color: '#fff', background: '#c0392b', padding: '2px 8px', borderRadius: '4px' }}>ВЫ ПОГИБЛИ</span>}
            </div>

            <div style={{ padding: '15px' }}>
                <CombatLog events={events} />

                <div style={{ marginTop: '15px' }}>
                    <CombatActions
                        onAction={onAction}
                        disabled={isPlayerDead || isEnemyDead || isLoading}
                    />
                </div>
            </div>
        </div>
    );
}