import React, { useEffect, useState } from 'react';
import { inventoryApi } from '../api/inventoryApi';
import Inventory from '../components/inventory/Inventory';
import Loading from '../components/common/Loading';
import ErrorMessage from '../components/common/ErrorMessage';

export default function InventoryPage() {
    const [items, setItems] = useState<any[]>([]); // Используем any для MVP, так как бэкенд отдает структуру PlayerItem
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchInventory();
    }, []);

    const fetchInventory = async () => {
        setLoading(true);
        setError(null);
        try {
            const data: any = await inventoryApi.getInventory();
            // Маппим данные с бэкенда под наш интерфейс
            const mappedItems = data.map((pi: any) => ({
                id: pi.id,
                name: pi.template.name,
                type: pi.template.type,
                description: `Статы: ${pi.template.statBudget} | Экипировано: ${pi.isEquipped ? 'Да' : 'Нет'}`,
                quantity: pi.amount
            }));
            setItems(mappedItems);
        } catch (err: any) {
            setError('Не удалось заглянуть в рюкзак');
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <Loading text="Перебираем вещи..." />;

    return (
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px' }}>
            <h1 style={{ color: '#2c3e50', borderBottom: '3px solid #e74c3c', paddingBottom: '10px' }}>🎒 Инвентарь</h1>
            <ErrorMessage message={error} />
            <Inventory items={items} />
        </div>
    );
}