import React from 'react';
import type { Item } from '../../types/inventory';

interface InventoryItemProps {
    item: Item;
    onClick?: (item: Item) => void;
}

export default function InventoryItem({ item, onClick }: InventoryItemProps) {
    return (
        <div
            onClick={() => onClick && onClick(item)}
            style={{
                border: '1px solid #dcdcdc',
                padding: '12px',
                borderRadius: '8px',
                cursor: onClick ? 'pointer' : 'default',
                backgroundColor: '#fff',
                color: '#333',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                textAlign: 'center',
                boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                transition: 'transform 0.2s ease'
            }}
            onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
            onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>{item.name}</div>
            <div style={{ fontSize: '0.8rem', color: '#888', marginBottom: '10px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{item.type}</div>
            <div style={{ fontSize: '0.9rem', flexGrow: 1, color: '#555' }}>{item.description}</div>

            <div style={{ marginTop: '12px', fontWeight: 'bold', backgroundColor: '#f0f0f0', padding: '4px 12px', borderRadius: '12px', fontSize: '0.85rem' }}>
                Кол-во: {item.quantity}
            </div>
        </div>
    );
}