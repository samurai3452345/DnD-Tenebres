import React, { useState } from 'react';
import Button from '../common/Button';
import type { CombatAction } from '../../types/combat';

interface CombatActionsProps {
    onAction: (action: CombatAction, targetId?: number) => void;
    disabled?: boolean;
}

export default function CombatActions({ onAction, disabled }: CombatActionsProps) {
    const [targetId, setTargetId] = useState("");

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', padding: '15px', background: '#f5f5f5', borderRadius: '8px' }}>
            <div style={{ display: 'flex', gap: '10px' }}>
                <Button variant="primary" disabled={disabled} onClick={() => onAction('ATTACK')} style={{ flex: 1 }}>
                    ⚔️ Атаковать
                </Button>
                <Button variant="secondary" disabled={disabled} onClick={() => onAction('FLEE')} style={{ flex: 1 }}>
                    🏃 Сбежать
                </Button>
            </div>
            <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginTop: '5px' }}>
                <input
                    type="number"
                    placeholder="ID зелья или способности"
                    value={targetId}
                    onChange={(e) => setTargetId(e.target.value)}
                    disabled={disabled}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', flexGrow: 1 }}
                />
                <Button
                    variant="primary"
                    disabled={disabled || !targetId}
                    onClick={() => onAction('USE_POTION', Number(targetId))}
                >
                    🧪 Выпить
                </Button>
                <Button
                    variant="primary"
                    disabled={disabled || !targetId}
                    onClick={() => onAction('CAST_SPELL', Number(targetId))}
                >
                    ✨ Каст
                </Button>
            </div>
        </div>
    );
}
