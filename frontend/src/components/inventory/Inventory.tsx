import React from 'react';
import type { Item } from '../../types/inventory';
import InventoryItem from './InventoryItem';

interface InventoryProps {
    items: Item[];
    onItemClick?: (item: Item) => void;
}

export default function Inventory({ items, onItemClick }: InventoryProps) {
    if (!items || items.length === 0) {
        return (
            <div style={{ padding: '30px', textAlign: 'center', color: '#777', backgroundColor: '#f9f9f9', borderRadius: '8px', border: '1px dashed #ccc' }}>
                Ваш инвентарь пуст.
            </div>
        );
    }

    return (
        <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))',
            gap: '15px',
            padding: '15px',
            backgroundColor: '#f4f4f4',
            borderRadius: '8px'
        }}>
            {items.map((item) => (
                <InventoryItem key={item.id} item={item} onClick={onItemClick} />
            ))}
        </div>
    );
}