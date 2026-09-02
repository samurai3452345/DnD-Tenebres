import React from 'react';

export default function Loading({ text = "Загрузка..." }: { text?: string }) {
    return (
        <div className="loading-container" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div className="loading-spinner">⏳</div>
            <span className="loading-text">{text}</span>
        </div>
    );
}