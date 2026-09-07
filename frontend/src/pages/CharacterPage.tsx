import React, { useState } from 'react';
import { usePlayer } from '../hooks/usePlayer';
import Button from '../components/common/Button';
import Loading from '../components/common/Loading';
import ErrorMessage from '../components/common/ErrorMessage';

export default function CharacterPage() {
    const { player, loading, error, refreshPlayer, allocateStats } = usePlayer();

    // Локальное состояние для распределяемых очков
    const [addedStats, setAddedStats] = useState({
        addStrength: 0,
        addDexterity: 0,
        addConstitution: 0,
        addIntelligence: 0,
        addWisdom: 0,
        addCharisma: 0,
    });

    const [isSaving, setIsSaving] = useState(false);
    const [actionMessage, setActionMessage] = useState<string | null>(null);

    if (loading) return <div style={{ padding: '20px' }}><Loading text="Загрузка характеристик..." /></div>;
    if (error || !player) return <div style={{ padding: '20px' }}><ErrorMessage message={error} /></div>;

    const totalAdded = Object.values(addedStats).reduce((sum, val) => sum + val, 0);
    const availablePoints = player.statPoints - totalAdded;

    const handleAdd = (stat: keyof typeof addedStats, direction: number) => {
        setAddedStats(prev => {
            const newValue = prev[stat] + direction;
            if (newValue < 0) return prev; // Нельзя уйти в минус
            if (direction > 0 && availablePoints <= 0) return prev; // Нельзя потратить больше, чем есть

            return { ...prev, [stat]: newValue };
        });
    };

    const handleSave = async () => {
        if (totalAdded === 0) return;
        setIsSaving(true);
        setActionMessage(null);

        const success = await allocateStats(addedStats);
        if (success) {
            setActionMessage("Характеристики успешно улучшены!");
            setAddedStats({ addStrength: 0, addDexterity: 0, addConstitution: 0, addIntelligence: 0, addWisdom: 0, addCharisma: 0 });
            refreshPlayer();
        } else {
            setActionMessage("Не удалось сохранить характеристики.");
        }
        setIsSaving(false);
    };

    const StatRow = ({ label, statKey, currentValue }: { label: string, statKey: keyof typeof addedStats, currentValue: number }) => (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid #eee' }}>
            <span style={{ fontSize: '1.1rem', width: '120px' }}>{label}</span>
            <span style={{ fontSize: '1.2rem', fontWeight: 'bold', width: '40px', textAlign: 'center' }}>
                {currentValue} <span style={{ color: '#27ae60' }}>{addedStats[statKey] > 0 ? `+${addedStats[statKey]}` : ''}</span>
            </span>
            <div style={{ display: 'flex', gap: '5px' }}>
                <Button variant="secondary" onClick={() => handleAdd(statKey, -1)} disabled={addedStats[statKey] === 0}>-</Button>
                <Button variant="primary" onClick={() => handleAdd(statKey, 1)} disabled={availablePoints === 0}>+</Button>
            </div>
        </div>
    );

    return (
        <div style={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
            <h1 style={{ color: '#2c3e50', borderBottom: '3px solid #3498db', paddingBottom: '10px' }}>Улучшение персонажа</h1>

            {actionMessage && <div style={{ padding: '10px', background: '#d4edda', color: '#155724', borderRadius: '4px', marginBottom: '15px' }}>{actionMessage}</div>}

            <div style={{ background: '#f8f9fa', padding: '15px', borderRadius: '8px', marginBottom: '20px', display: 'flex', justifyContent: 'space-between', fontSize: '1.1rem' }}>
                <span>Очки характеристик: <strong>{availablePoints}</strong></span>
                <span>Уровень: <strong>{player.level}</strong></span>
            </div>

            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', border: '1px solid #ddd' }}>
                <StatRow label="💪 Сила" statKey="addStrength" currentValue={player.stats.strength} />
                <StatRow label="🏃 Ловкость" statKey="addDexterity" currentValue={player.stats.dexterity} />
                <StatRow label="🛡️ Телослож." statKey="addConstitution" currentValue={player.stats.constitution} />
                <StatRow label="🧠 Интеллект" statKey="addIntelligence" currentValue={player.stats.intelligence} />
                <StatRow label="🦉 Мудрость" statKey="addWisdom" currentValue={player.stats.wisdom} />
                <StatRow label="✨ Харизма" statKey="addCharisma" currentValue={player.stats.charisma} />
            </div>

            <div style={{ marginTop: '20px', textAlign: 'right' }}>
                <Button onClick={handleSave} disabled={totalAdded === 0 || isSaving} style={{ padding: '10px 20px', fontSize: '1.1rem' }}>
                    {isSaving ? 'Сохранение...' : 'Подтвердить изменения'}
                </Button>
            </div>
        </div>
    );
}