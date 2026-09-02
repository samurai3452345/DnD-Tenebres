import React from 'react';

interface ManaBarProps {
    currentMp: number;
    maxMp: number;
}

export default function ManaBar({ currentMp, maxMp }: ManaBarProps) {
    const percentage = maxMp > 0 ? Math.max(0, Math.min(100, (currentMp / maxMp) * 100)) : 0;

    return (
        <div style={{ marginBottom: '10px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px', fontSize: '0.9rem' }}>
                <strong>Мана</strong>
                <span>{currentMp} / {maxMp}</span>
            </div>
            <div style={{ background: '#e0e0e0', borderRadius: '4px', height: '16px', overflow: 'hidden' }}>
                <div
                    style={{
                        background: '#3498db',
                        width: `${percentage}%`,
                        height: '100%',
                        transition: 'width 0.3s ease'
                    }}
                />
            </div>
        </div>
    );
}