import { useState, useCallback, useEffect } from 'react';
import { playerApi } from '../api/playerApi';
import type { Player, StatAllocationRequest } from '../types/player';

export function usePlayer() {
    const [player, setPlayer] = useState<Player | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPlayer = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await playerApi.getMe();
            setPlayer(data);
        } catch (err: any) {
            setError('Не удалось загрузить данные персонажа');
        } finally {
            setLoading(false);
        }
    }, []);

    const allocateStats = async (request: StatAllocationRequest) => {
        try {
            const updatedPlayer = await playerApi.allocateStats(request);
            setPlayer(updatedPlayer);
            return true;
        } catch (err: any) {
            setError('Ошибка при распределении характеристик');
            return false;
        }
    };

    useEffect(() => {
        fetchPlayer();
    }, [fetchPlayer]);

    return { player, loading, error, refreshPlayer: fetchPlayer, allocateStats };
}