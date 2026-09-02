import React, { useEffect, useState, useCallback } from 'react';
import { questApi } from '../api/questApi';
import type { QuestTemplate, PlayerQuest } from '../types/quest';
import QuestList from '../components/quests/QuestList';
import Loading from '../components/common/Loading';
import ErrorMessage from '../components/common/ErrorMessage';

export default function QuestsPage() {
    const [available, setAvailable] = useState<QuestTemplate[]>([]);
    const [active, setActive] = useState<PlayerQuest[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchQuests = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const [availableData, activeData] = await Promise.all([
                questApi.getAvailable(),
                questApi.getActive()
            ]);
            setAvailable(availableData);
            setActive(activeData);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Не удалось загрузить квесты. Вы точно в Гильдии Авантюристов?');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchQuests();
    }, [fetchQuests]);

    const handleAccept = async (id: number) => {
        try {
            await questApi.accept(id);
            fetchQuests(); // Перезагружаем списки после взятия
        } catch (err: any) {
            alert(err.response?.data?.message || 'Ошибка при взятии квеста');
        }
    };

    const handleTurnIn = async (id: number) => {
        try {
            await questApi.turnIn(id);
            fetchQuests();
        } catch (err: any) {
            alert(err.response?.data?.message || 'Ошибка при сдаче квеста');
        }
    };

    if (loading) return <Loading text="Ищем подходящие задания..." />;

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
            <h1 style={{ color: '#2c3e50', borderBottom: '3px solid #e74c3c', paddingBottom: '10px' }}>📜 Гильдия Авантюристов</h1>
            <ErrorMessage message={error} />

            {!error && (
                <>
                    <QuestList title="Текущие задания" playerQuests={active} onTurnIn={handleTurnIn} />
                    <QuestList title="Доска объявлений" templates={available} onAccept={handleAccept} />
                </>
            )}
        </div>
    );
}