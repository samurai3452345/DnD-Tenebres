import React from 'react';

interface HealthBarProps {
    currentHp: number;
    maxHp: number;
}

export default function HealthBar({ currentHp, maxHp }: HealthBarProps) {
    const percentage = maxHp > 0 ? Math.max(0, Math.min(100, (currentHp / maxHp) * 100)) : 0;

    return (
        <div style={{ marginBottom: '10px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px', fontSize: '0.9rem' }}>
                <strong>Здоровье</strong>
                <span>{currentHp} / {maxHp}</span>
            </div>
            <div style={{ background: '#e0e0e0', borderRadius: '4px', height: '16px', overflow: 'hidden' }}>
                <div
                    style={{
                        background: '#e74c3c',
                        width: `${percentage}%`,
                        height: '100%',
                        transition: 'width 0.3s ease'
                    }}
                />
            </div>
        </div>
    );
}