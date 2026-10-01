import React, { useState, useEffect } from 'react';
import { usePlayer } from '../hooks/usePlayer';
import { useCombat } from '../hooks/useCombat';
import { explorationApi } from '../api/explorationApi';
import { restApi } from '../api/restApi';
import PlayerStats from '../components/player/PlayerStats';
import CombatPanel from '../components/combat/CombatPanel';
import Button from '../components/common/Button';
import Loading from '../components/common/Loading';
import ErrorMessage from '../components/common/ErrorMessage';

export default function GamePage() {
    const { player, loading: playerLoading, error: playerError, refreshPlayer } = usePlayer();
    const { events, isPlayerDead, isEnemyDead, executeTurn, loading: combatLoading, resetCombat } = useCombat();

    const [actionLog, setActionLog] = useState<string>('Добро пожаловать в мир Tenebres!');
    const [activeMonsterId, setActiveMonsterId] = useState<number | null>(null);

    // Если бэкенд говорит, что мы уже в бою при загрузке страницы
    useEffect(() => {
        if (player?.activeCombatMonsterId) {
            setActiveMonsterId(player.activeCombatMonsterId);
        }
    }, [player]);

    const handleHunt = async () => {
        try {
            const res = await explorationApi.hunt();
            setActionLog(res.message);
            if (res.encounterMonsterId) {
                setActiveMonsterId(res.encounterMonsterId);
                resetCombat();
            }
            refreshPlayer();
        } catch (err: any) {
            setActionLog(err.response?.data?.message || 'Ошибка при поиске врагов');
        }
    };

    const handleSearch = async () => {
        try {
            const res = await explorationApi.search();
            setActionLog(res.message);
            refreshPlayer();
        } catch (err: any) {
            setActionLog(err.response?.data?.message || 'Ошибка обыска');
        }
    };

    const handleRest = async (type: 'short' | 'long') => {
        try {
            const res: any = type === 'short' ? await restApi.shortRest() : await restApi.longRest();
            setActionLog(res.message);
            if (res.status === 'AMBUSH' && res.combatLog) {
                setActiveMonsterId(999); // Условный ID для засады
                resetCombat();
            }
            refreshPlayer();
        } catch (err: any) {
            setActionLog(err.response?.data?.message || 'Отдохнуть не вышло');
        }
    };

    const handleCombatAction = async (action: any, targetName?: string) => {
        if (!activeMonsterId) return;
        await executeTurn(activeMonsterId, action, targetName);
        refreshPlayer();
    };

    const finishCombat = () => {
        setActiveMonsterId(null);
        resetCombat();
        refreshPlayer();
        setActionLog('Бой окончен. Вы можете продолжать путь.');
    };

    if (playerLoading) return <div style={{ padding: '50px', textAlign: 'center' }}><Loading text="Загрузка мира..." /></div>;
    if (playerError || !player) return <div style={{ padding: '50px' }}><ErrorMessage message={playerError || "Не удалось загрузить профиль"} /></div>;

    return (
        <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '20px', display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '20px' }}>

            {/* Левая колонка: Статы */}
            <div>
                <PlayerStats player={player} />

                {/* Панель исследования (прячем, если идет бой) */}
                {!activeMonsterId && (
                    <div style={{ marginTop: '20px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        <h3 style={{ margin: '0 0 10px 0', color: '#2c3e50' }}>Действия</h3>
                        <Button onClick={handleHunt}>⚔️ Искать врагов (Охота)</Button>
                        <Button onClick={handleSearch} variant="secondary">🔍 Обыскать локацию</Button>
                        <Button onClick={() => handleRest('short')} variant="secondary">🏕️ Короткий привал</Button>
                        <Button onClick={() => handleRest('long')} variant="secondary">🛏️ Сон в таверне</Button>
                    </div>
                )}
            </div>

            {/* Правая колонка: Бой или Лог событий */}
            <div>
                {activeMonsterId ? (
                    <div>
                        <CombatPanel
                            monsterName="Неизвестный противник"
                            events={events}
                            isPlayerDead={isPlayerDead}
                            isEnemyDead={isEnemyDead}
                            isLoading={combatLoading}
                            onAction={handleCombatAction}
                        />
                        {(isPlayerDead || isEnemyDead) && (
                            <Button onClick={finishCombat} style={{ marginTop: '15px', width: '100%' }}>
                                Вернуться на экран действий
                            </Button>
                        )}
                    </div>
                ) : (
                    <div style={{ background: '#ecf0f1', padding: '20px', borderRadius: '8px', minHeight: '200px', border: '1px solid #bdc3c7' }}>
                        <h3 style={{ margin: '0 0 15px 0', color: '#7f8c8d' }}>Журнал событий</h3>
                        <p style={{ fontSize: '1.1rem', color: '#2c3e50', lineHeight: '1.5' }}>
                            {actionLog}
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
}