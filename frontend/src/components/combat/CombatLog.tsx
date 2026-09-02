import React, { useEffect, useRef } from 'react';
import type { CombatEvent } from '../../types/combat';

interface CombatLogProps {
    events: CombatEvent[];
}

export default function CombatLog({ events }: CombatLogProps) {
    const logEndRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        logEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [events]);

    if (!events || events.length === 0) {
        return (
            <div style={{ padding: '20px', textAlign: 'center', color: '#888', fontStyle: 'italic', background: '#2c3e50', borderRadius: '8px' }}>
                Бой еще не начался...
            </div>
        );
    }

    return (
        <div style={{
            height: '250px',
            overflowY: 'auto',
            background: '#2c3e50',
            color: '#ecf0f1',
            padding: '10px 15px',
            borderRadius: '8px',
            fontFamily: 'monospace',
            fontSize: '0.95rem'
        }}>
            {events.map((ev, index) => {
                let color = '#ecf0f1';
                if (ev.actionType.includes('CRIT')) color = '#f1c40f';
                else if (ev.actionType === 'MISS') color = '#95a5a6';
                else if (ev.actionType.includes('HEAL') || ev.actionType.includes('BUFF')) color = '#2ecc71';
                else if (ev.value > 0 || ev.actionType === 'DEATH') color = '#e74c3c';

                return (
                    <div key={index} style={{ marginBottom: '8px', borderBottom: '1px solid #34495e', paddingBottom: '6px' }}>
                        <span style={{ color: '#bdc3c7', fontSize: '0.85rem' }}>[{ev.actionType}]</span>{' '}
                        <strong>{ev.actor}</strong>{' '}
                        {ev.target && ev.target !== ev.actor && <span>&rarr; <strong>{ev.target}</strong></span>}{' '}
                        <span style={{ color }}>
                            {ev.value > 0 ? `(${ev.value}) ` : ''}
                            {ev.description}
                        </span>
                    </div>
                );
            })}
            <div ref={logEndRef} />
        </div>
    );
}