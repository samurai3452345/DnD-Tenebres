import React, { useState, useEffect } from 'react';
import { usePlayer } from '../hooks/usePlayer';
import { inventoryApi } from '../api/inventoryApi';
import { shopApi, type ShopOffer } from '../api/shopApi';
import Button from '../components/common/Button';
import Loading from '../components/common/Loading';
import ErrorMessage from '../components/common/ErrorMessage';

export default function ShopPage() {
    const { player, loading: playerLoading, error: playerError, refreshPlayer } = usePlayer();
    const [inventory, setInventory] = useState<any[]>([]);
    const [offers, setOffers] = useState<ShopOffer[]>([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<{ text: string, type: 'success' | 'error' } | null>(null);

    const loadInventory = async () => {
        try {
            const data: any = await inventoryApi.getInventory();
            setInventory(data.items ?? []);
        } catch (e) {
            console.error(e);
        }
    };

    useEffect(() => {
        loadInventory();
        shopApi.getOffers().then(setOffers).catch((error) => {
            console.error(error);
            setMessage({ text: 'Не удалось загрузить товары торговца', type: 'error' });
        });
    }, []);

    const handleBuy = async (offerId: number) => {
        setLoading(true);
        setMessage(null);
        try {
            const res = await shopApi.buyItem({ operationId: crypto.randomUUID(), offerId, amount: 1 });
            setMessage({ text: res.message, type: 'success' });
            refreshPlayer();
            loadInventory();
        } catch (err: any) {
            setMessage({ text: err.response?.data?.message || 'Ошибка при покупке', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const handleSell = async (playerItemId: number) => {
        setLoading(true);
        setMessage(null);
        try {
            const res = await shopApi.sellItem({ operationId: crypto.randomUUID(), playerItemId, amount: 1 });
            setMessage({ text: res.message, type: 'success' });
            refreshPlayer();
            loadInventory();
        } catch (err: any) {
            setMessage({ text: err.response?.data?.message || 'Ошибка при продаже', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    if (playerLoading) return <div style={{ padding: '20px' }}><Loading text="Идем к торговцу..." /></div>;
    if (playerError || !player) return <div style={{ padding: '20px' }}><ErrorMessage message={playerError} /></div>;

    // Игрок может продавать только то, что не надето
    const sellableItems = inventory.filter(item => !item.equipped && !item.locked);

    return (
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '3px solid #f1c40f', paddingBottom: '10px', marginBottom: '20px' }}>
                <h1 style={{ color: '#2c3e50', margin: 0 }}>💰 Гильдия Торговцев</h1>
                <div style={{ fontSize: '1.2rem', background: '#fdf1cc', padding: '5px 15px', borderRadius: '20px', border: '1px solid #f1c40f' }}>
                    Ваше золото: <strong>{player.gold} 🪙</strong>
                </div>
            </div>

            {message && (
                <div style={{ padding: '10px', borderRadius: '4px', marginBottom: '20px', background: message.type === 'success' ? '#d4edda' : '#f8d7da', color: message.type === 'success' ? '#155724' : '#721c24' }}>
                    {message.text}
                </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>

                {/* Витрина */}
                <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', border: '1px solid #dcdcdc' }}>
                    <h2 style={{ marginTop: 0, color: '#333' }}>Витрина (Купить)</h2>
                    {offers.map((offer) => (
                        <div key={offer.offerId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid #eee' }}>
                            <div>
                                <div style={{ fontWeight: 'bold' }}>{offer.name}</div>
                                <div style={{ fontSize: '0.85rem', color: '#777' }}>
                                    {offer.type} · {offer.rarity}{offer.minLevel > 1 ? ` · уровень ${offer.minLevel}` : ''}
                                </div>
                            </div>
                            <Button
                                onClick={() => handleBuy(offer.offerId)}
                                disabled={loading || !offer.available || player.gold < offer.unitPrice}
                                style={{ background: offer.available && player.gold >= offer.unitPrice ? '#27ae60' : '#ccc' }}
                            >
                                {offer.unitPrice} 🪙
                            </Button>
                        </div>
                    ))}
                </div>

                {/* Инвентарь для продажи */}
                <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', border: '1px solid #dcdcdc' }}>
                    <h2 style={{ marginTop: 0, color: '#333' }}>Ваши вещи (Продать)</h2>
                    {sellableItems.length === 0 ? (
                        <p style={{ color: '#999', fontStyle: 'italic' }}>Нет предметов для продажи (экипированные вещи продать нельзя).</p>
                    ) : (
                        sellableItems.map(item => (
                            <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid #eee' }}>
                                <div>
                                    <div style={{ fontWeight: 'bold' }}>{item.template.name}</div>
                                    <div style={{ fontSize: '0.85rem', color: '#777' }}>Кол-во: {item.amount}</div>
                                </div>
                                <Button variant="danger" onClick={() => handleSell(item.id)} disabled={loading}>
                                    Продать
                                </Button>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}
