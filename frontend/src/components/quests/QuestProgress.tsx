import React from 'react';

interface QuestProgressProps {
    current: number;
    target: number;
}

export default function QuestProgress({ current, target }: QuestProgressProps) {
    const percentage = target > 0 ? Math.min(100, (current / target) * 100) : 0;

    return (
        <div style={{ marginTop: '10px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '4px', color: '#555' }}>
                <span>Прогресс:</span>
                <span>{current} / {target}</span>
            </div>
            <div style={{ background: '#ecf0f1', borderRadius: '4px', height: '10px', overflow: 'hidden' }}>
                <div style={{ background: '#27ae60', width: `${percentage}%`, height: '100%', transition: 'width 0.3s' }} />
            </div>
        </div>
    );
}