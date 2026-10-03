import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { playerApi } from "../api/playerApi";
import { useAuth } from "../context/AuthContext";
import type { CharacterSummary } from "../types/player";
import { getAuthError } from "../utils/authError";

const goldFormatter = new Intl.NumberFormat("ru-RU");

export default function CharacterSelectionPage() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [characters, setCharacters] = useState<CharacterSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectingId, setSelectingId] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let active = true;

        playerApi.getCharacters()
            .then((data) => {
                if (active) setCharacters(data);
            })
            .catch((requestError) => {
                if (active) setError(getAuthError(requestError, "Не удалось загрузить персонажей."));
            })
            .finally(() => {
                if (active) setLoading(false);
            });

        return () => {
            active = false;
        };
    }, []);

    const selectCharacter = async (playerId: number) => {
        setSelectingId(playerId);
        setError(null);
        try {
            const response = await playerApi.selectCharacter(playerId);
            const remember = localStorage.getItem("token") !== null;
            login(response.token, remember);
            navigate("/", { replace: true });
        } catch (requestError) {
            setError(getAuthError(requestError, "Не удалось выбрать персонажа."));
            setSelectingId(null);
        }
    };

    return (
        <main className="character-selection-scene">
            <div className="character-selection-scene__veil" aria-hidden="true" />
            <section className="character-selection" aria-labelledby="character-selection-title">
                <h1 id="character-selection-title" className="sr-only">Выбор персонажа</h1>
                <img
                    className="character-selection__frame"
                    src="/assets/character-selection/frame.png"
                    alt=""
                    aria-hidden="true"
                />

                <div className="character-selection__list" aria-live="polite">
                    {loading && <p className="character-selection__status">Собираем ваших героев…</p>}
                    {!loading && error && <p className="character-selection__status character-selection__status--error" role="alert">{error}</p>}
                    {!loading && !error && characters.length === 0 && (
                        <p className="character-selection__status">У этого аккаунта пока нет персонажей.</p>
                    )}

                    {!loading && characters.map((character) => (
                        <article className="character-card" key={character.playerId}>
                            <img src="/assets/character-selection/character-card.png" alt="" aria-hidden="true" />
                            <strong className="character-card__value character-card__name">{character.playerName}</strong>
                            <span className="character-card__value character-card__level">{character.level}</span>
                            <span className="character-card__value character-card__location">
                                {character.locationName ?? "Неизвестно"}
                            </span>
                            <span className="character-card__value character-card__gold">{goldFormatter.format(character.gold)}</span>
                            <span className="character-card__value character-card__strength">{character.stats.strength}</span>
                            <span className="character-card__value character-card__dexterity">{character.stats.dexterity}</span>
                            <span className="character-card__value character-card__constitution">{character.stats.constitution}</span>
                            <span className="character-card__value character-card__intelligence">{character.stats.intelligence}</span>
                            <span className="character-card__value character-card__wisdom">{character.stats.wisdom}</span>
                            <span className="character-card__value character-card__charisma">{character.stats.charisma}</span>
                            <button
                                className="character-card__select"
                                type="button"
                                aria-label={`Выбрать персонажа ${character.playerName}`}
                                disabled={selectingId !== null}
                                onClick={() => selectCharacter(character.playerId)}
                            >
                                <span className="sr-only">
                                    {selectingId === character.playerId ? "Выбираем персонажа" : "Выбрать персонажа"}
                                </span>
                            </button>
                        </article>
                    ))}
                </div>

                <button
                    className="character-selection__create"
                    type="button"
                    aria-label="Создать персонажа"
                    onClick={() => navigate("/create-character")}
                >
                    <span className="sr-only">Создать персонажа</span>
                </button>
            </section>
        </main>
    );
}
