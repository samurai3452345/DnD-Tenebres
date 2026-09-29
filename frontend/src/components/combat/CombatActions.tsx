import React, { useState } from 'react';
import Button from '../common/Button';
import type { CombatAction } from '../../types/combat';

interface CombatActionsProps {
    onAction: (action: CombatAction, targetName?: string) => void;
    disabled?: boolean;
}

export default function CombatActions({ onAction, disabled }: CombatActionsProps) {
    const [targetName, setTargetName] = useState("");

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
                    type="text"
                    placeholder="Название зелья или заклинания..."
                    value={targetName}
                    onChange={(e) => setTargetName(e.target.value)}
                    disabled={disabled}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', flexGrow: 1 }}
                />
                <Button
                    variant="primary"
                    disabled={disabled || !targetName.trim()}
                    onClick={() => onAction('USE_POTION', targetName.trim())}
                >
                    🧪 Выпить
                </Button>
                <Button
                    variant="primary"
                    disabled={disabled || !targetName.trim()}
                    onClick={() => onAction('CAST_SPELL', targetName.trim())}
                >
                    ✨ Каст
                </Button>
            </div>
        </div>
    );
}