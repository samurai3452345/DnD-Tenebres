import { useMemo, useState, type CSSProperties, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";

import { playerApi } from "../api/playerApi";
import { useAuth } from "../context/AuthContext";
import type { PlayerStats } from "../types/player";
import { getAuthError } from "../utils/authError";

const MIN_STAT = 8;
const MAX_STAT = 15;
const TOTAL_POINTS = 27;
const POINT_COST: Record<number, number> = {
    8: 0,
    9: 1,
    10: 2,
    11: 3,
    12: 4,
    13: 5,
    14: 7,
    15: 9,
};

const STAT_KEYS: Array<keyof PlayerStats> = [
    "strength",
    "dexterity",
    "constitution",
    "intelligence",
    "wisdom",
    "charisma",
];

const STAT_TOPS = [23.15, 36.3, 49.3, 62.3, 75.1, 87.9];

const INITIAL_STATS: PlayerStats = {
    strength: MIN_STAT,
    dexterity: MIN_STAT,
    constitution: MIN_STAT,
    intelligence: MIN_STAT,
    wisdom: MIN_STAT,
    charisma: MIN_STAT,
};

export default function CharacterCreationPage() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [name, setName] = useState("");
    const [stats, setStats] = useState<PlayerStats>(INITIAL_STATS);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const spentPoints = useMemo(
        () => STAT_KEYS.reduce((total, key) => total + POINT_COST[stats[key]], 0),
        [stats],
    );
    const remainingPoints = TOTAL_POINTS - spentPoints;

    const changeStat = (key: keyof PlayerStats, direction: -1 | 1) => {
        setStats((current) => {
            const value = current[key];
            const nextValue = value + direction;
            if (nextValue < MIN_STAT || nextValue > MAX_STAT) return current;

            if (direction > 0) {
                const price = POINT_COST[nextValue] - POINT_COST[value];
                if (price > remainingPoints) return current;
            }

            return { ...current, [key]: nextValue };
        });
        setError(null);
    };

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const normalizedName = name.trim();

        if (normalizedName.length < 2 || normalizedName.length > 16) {
            setError("Имя героя должно содержать от 2 до 16 символов.");
            return;
        }
        if (remainingPoints !== 0) {
            setError(`Распределите все очки характеристик. Осталось: ${remainingPoints}.`);
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const response = await playerApi.create({ name: normalizedName, ...stats });
            const remembered = localStorage.getItem("token") !== null;
            login(response.token, remembered);
            navigate("/characters", { replace: true });
        } catch (requestError) {
            setError(getAuthError(requestError, "Не удалось создать персонажа."));
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="character-creation-scene">
            <div className="character-creation-scene__veil" aria-hidden="true" />
            <form className="character-creator" onSubmit={handleSubmit} noValidate>
                <img className="character-creator__frame" src="/assets/character-creation/frame.png" alt="" />

                <div className="character-name-field">
                    <img src="/assets/character-creation/name-field.png" alt="" aria-hidden="true" />
                    <label className="sr-only" htmlFor="character-name">Имя героя</label>
                    <input
                        id="character-name"
                        value={name}
                        onChange={(event) => {
                            setName(event.target.value.slice(0, 16));
                            setError(null);
                        }}
                        placeholder="Имя героя"
                        autoComplete="off"
                        maxLength={16}
                        autoFocus
                    />
                    <span className="character-name-field__count">{name.length}/16</span>
                </div>

                <div className="character-stats-panel">
                    <img src="/assets/character-creation/stats-panel.png" alt="" aria-hidden="true" />
                    <strong className="character-stats-panel__remaining">{remainingPoints}</strong>
                    {STAT_KEYS.map((key, index) => (
                        <div
                            className="character-stat-control"
                            style={{ "--stat-top": `${STAT_TOPS[index]}%` } as CSSProperties}
                            key={key}
                        >
                            <output aria-label={`Значение характеристики ${key}`}>{stats[key]}</output>
                            <button
                                className="character-stat-control__minus"
                                type="button"
                                aria-label={`Уменьшить ${key}`}
                                disabled={stats[key] <= MIN_STAT}
                                onClick={() => changeStat(key, -1)}
                            />
                            <button
                                className="character-stat-control__plus"
                                type="button"
                                aria-label={`Увеличить ${key}`}
                                disabled={
                                    stats[key] >= MAX_STAT
                                    || POINT_COST[stats[key] + 1] - POINT_COST[stats[key]] > remainingPoints
                                }
                                onClick={() => changeStat(key, 1)}
                            />
                        </div>
                    ))}
                </div>

                <div className="character-creator__message" aria-live="polite">
                    {error && <p role="alert">{error}</p>}
                </div>
                <button className="character-creator__submit" type="submit" disabled={loading}>
                    <span>{loading ? "Создаём героя…" : "Создать персонажа"}</span>
                </button>
            </form>
        </main>
    );
}
