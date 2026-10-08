import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { explorationApi } from "../api/explorationApi";
import { locationApi } from "../api/locationApi";
import { questApi } from "../api/questApi";
import { restApi } from "../api/restApi";
import CombatPanel from "../components/combat/CombatPanel";
import ErrorMessage from "../components/common/ErrorMessage";
import Loading from "../components/common/Loading";
import { useCombat } from "../hooks/useCombat";
import { usePlayer } from "../hooks/usePlayer";
import type { CombatActionRequest } from "../types/combat";
import type { Location, LocationConnection } from "../types/location";
import type { PlayerQuest, QuestSource } from "../types/quest";

const locationFrames: Record<Location["type"], string> = {
    SAFE_ZONE: "/assets/game/location-safe.png",
    NEUTRAL: "/assets/game/location-hostile.png",
    DANGEROUS: "/assets/game/location-hostile.png",
};

const locationBackgrounds: Record<string, string> = {
    city_merch_guild: "/assets/game/location-merchant-guild.png",
    city_adv_guild: "/assets/game/location-adventurers-guild.png",
    city_tavern: "/assets/game/location-north-hearth-tavern.png",
    city_gates: "/assets/game/location-main-gates.png",
    forest_edge: "/assets/game/location-forest-edge.png",
    forest_goblin_camp: "/assets/game/location-goblin-camp.png",
    forest_wolf_trail: "/assets/game/location-wolf-trail.png",
    crypt_entrance: "/assets/game/location-crypt-entrance.png",
    crypt_corridor: "/assets/game/location-crypt-corridor.png",
    crypt_central: "/assets/game/location-crypt-central.png",
    crypt_library: "/assets/game/location-crypt-library.png",
    crypt_training: "/assets/game/location-crypt-training.png",
    crypt_armory: "/assets/game/location-crypt-armory.png",
    crypt_boss_room: "/assets/game/location-crypt-throne-room.png",
};

const zoneNameOverrides: Record<string, string> = {
    "Зеленый лес": "Сумрачный лес",
    "Зелёный лес": "Сумрачный лес",
};

const travelIcons: Record<Location["type"], string> = {
    SAFE_ZONE: "/assets/game/travel-icon-safe.png",
    NEUTRAL: "/assets/game/travel-icon-neutral.png",
    DANGEROUS: "/assets/game/travel-icon-dangerous.png",
};

function travelIconFor(connection: LocationConnection) {
    if (connection.bossRoom || connection.id === "crypt_boss_room") {
        return "/assets/game/travel-icon-boss.png";
    }
    if (connection.type === "SAFE_ZONE" || connection.id.startsWith("city_")) {
        return travelIcons.SAFE_ZONE;
    }
    if (connection.type === "DANGEROUS" || connection.id.startsWith("crypt_")) {
        return travelIcons.DANGEROUS;
    }
    return travelIcons.NEUTRAL;
}

const questSourceLabels: Record<QuestSource, string> = {
    MAIN_STORY: "Сюжетное задание",
    NPC: "Задание персонажа",
    GUILD: "Задание гильдии",
};

function requestErrorMessage(error: unknown, fallback: string) {
    const candidate = error as { response?: { data?: { message?: string } } };
    return candidate.response?.data?.message ?? fallback;
}

export default function GamePage() {
    const navigate = useNavigate();
    const routeLocation = useLocation();
    const enteredFromCharacterSelection = useRef(
        Boolean((routeLocation.state as { showWelcome?: boolean } | null)?.showWelcome)
    );
    const { player, loading: playerLoading, error: playerError, refreshPlayer } = usePlayer();
    const {
        state: combatState,
        enemyName,
        loadCurrent: loadCurrentCombat,
        executeAction: executeCombatAction,
        adoptState: adoptCombatState,
        loading: combatLoading,
        resetCombat,
    } = useCombat();
    const [location, setLocation] = useState<Location | null>(null);
    const [quests, setQuests] = useState<PlayerQuest[]>([]);
    const [worldLoading, setWorldLoading] = useState(true);
    const [worldError, setWorldError] = useState<string | null>(null);
    const [message, setMessage] = useState<string | null>(
        enteredFromCharacterSelection.current ? "Добро пожаловать в мир Tenebres!" : null
    );
    const [combatOpen, setCombatOpen] = useState(false);
    const [travelOpen, setTravelOpen] = useState(false);
    const [busyAction, setBusyAction] = useState<string | null>(null);

    const refreshWorld = useCallback(async () => {
        setWorldError(null);
        try {
            const [currentLocation, activeQuests] = await Promise.all([
                locationApi.getCurrent(),
                questApi.getActive(),
            ]);
            setLocation(currentLocation);
            setQuests(activeQuests);
        } catch (error) {
            setWorldError(requestErrorMessage(error, "Не удалось загрузить данные игрового мира."));
        } finally {
            setWorldLoading(false);
        }
    }, []);

    useEffect(() => {
        refreshWorld();
    }, [refreshWorld]);

    useEffect(() => {
        if (enteredFromCharacterSelection.current) {
            navigate("/", { replace: true, state: null });
            enteredFromCharacterSelection.current = false;
        }
    }, [navigate]);

    useEffect(() => {
        if (!message) return;
        const timeoutId = window.setTimeout(() => setMessage(null), 4500);
        return () => window.clearTimeout(timeoutId);
    }, [message]);

    useEffect(() => {
        if (!player?.activeCombatMonsterId) return;
        setCombatOpen(true);
        void loadCurrentCombat().catch((error) => {
            setCombatOpen(false);
            setMessage(requestErrorMessage(error, "Не удалось восстановить активный бой."));
        });
    }, [player?.activeCombatMonsterId, loadCurrentCombat]);

    const runAction = async (key: string, action: () => Promise<{ message: string; encounterMonsterId?: number | null }>) => {
        setBusyAction(key);
        try {
            const response = await action();
            setMessage(response.message);
            if (response.encounterMonsterId) {
                resetCombat();
                setCombatOpen(true);
                await loadCurrentCombat();
            }
            await Promise.all([refreshPlayer(), refreshWorld()]);
        } catch (error) {
            setMessage(requestErrorMessage(error, "Действие выполнить не удалось."));
        } finally {
            setBusyAction(null);
        }
    };

    const handleRest = async (kind: "short" | "long") => {
        setBusyAction(kind);
        try {
            const response = kind === "short" ? await restApi.shortRest() : await restApi.longRest();
            setMessage(response.message);
            if (response.encounter) {
                adoptCombatState(response.encounter);
                setCombatOpen(true);
            } else if (response.isAmbushed) {
                resetCombat();
                setCombatOpen(true);
                await loadCurrentCombat();
            }
            await Promise.all([refreshPlayer(), refreshWorld()]);
        } catch (error) {
            setMessage(requestErrorMessage(error, "Отдохнуть не вышло."));
        } finally {
            setBusyAction(null);
        }
    };

    const handleTravel = async (connection: LocationConnection) => {
        if (!connection.open) {
            setMessage(connection.blockedReasons.join(". ") || "Путь пока закрыт.");
            return;
        }
        setTravelOpen(false);
        await runAction("travel", () => explorationApi.travel({ targetLocationId: connection.id }));
    };

    const finishCombat = async () => {
        setCombatOpen(false);
        resetCombat();
        setMessage("Бой окончен. Можно продолжать путь.");
        await Promise.all([refreshPlayer(), refreshWorld()]);
    };

    const handleCombatAction = async (request: CombatActionRequest) => {
        try {
            await executeCombatAction(request);
            await refreshPlayer();
        } catch (error) {
            setMessage(requestErrorMessage(error, "Не удалось выполнить боевое действие."));
        }
    };

    const displayedQuests = useMemo(() => quests.slice(0, 5), [quests]);

    if (playerLoading || worldLoading) {
        return <main className="game-dashboard game-dashboard--centered"><Loading text="Загрузка мира..." /></main>;
    }

    if (playerError || worldError || !player || !location) {
        return (
            <main className="game-dashboard game-dashboard--centered">
                <ErrorMessage message={playerError || worldError || "Не удалось загрузить игровой экран."} />
            </main>
        );
    }

    const actionsDisabled = busyAction !== null || combatOpen || player.activeCombatMonsterId !== null;
    const allows = (action: string) => location.availableActions.includes(action);
    const locationBackground = locationBackgrounds[location.id] ?? "/assets/game/main-background.png";
    const zoneName = zoneNameOverrides[location.zoneName] ?? location.zoneName;

    return (
        <main className="game-dashboard" style={{ backgroundImage: `url("${locationBackground}")` }}>
            <div className="game-dashboard__veil" aria-hidden="true" />

            <section className="game-player" aria-label="Профиль персонажа">
                <img src="/assets/game/player-panel.png" alt="" aria-hidden="true" />
                <button
                    className="game-player__avatar-button"
                    type="button"
                    aria-label="Открыть профиль персонажа"
                    title="Профиль персонажа"
                    onClick={() => navigate("/character")}
                />
                <div className="game-player__identity">
                    <strong>{player.playerName}</strong>
                    <span>Ур. {player.level}</span>
                </div>
                <div className="game-player__bar game-player__bar--hp">
                    <img src="/assets/game/player-health.png" alt="" aria-hidden="true" />
                    <span>{player.currentHp} / {player.maxHp}</span>
                </div>
                <div className="game-player__bar game-player__bar--mp">
                    <img src="/assets/game/player-mana.png" alt="" aria-hidden="true" />
                    <span>{player.currentMp} / {player.maxMp}</span>
                </div>
            </section>

            <section className="game-location" aria-label="Текущая локация">
                <img src={location.bossRoom ? "/assets/game/location-boss.png" : locationFrames[location.type]} alt="" aria-hidden="true" />
                <strong className="game-location__biome">{zoneName}</strong>
                <span className="game-location__name">{location.name}</span>
            </section>

            <section className="game-quests" aria-label="Активные задания">
                <img src="/assets/game/quests-panel.png" alt="" aria-hidden="true" />
                <h2>Задания</h2>
                <div className="game-quests__items">
                    {displayedQuests.map((playerQuest) => (
                        <article className="game-quest" key={playerQuest.id}>
                            <span
                                className={`game-quest__icon game-quest__icon--${playerQuest.quest.source.toLowerCase()}`}
                                title={questSourceLabels[playerQuest.quest.source]}
                            >
                                {playerQuest.quest.source === "MAIN_STORY" ? "◆" : playerQuest.quest.source === "NPC" ? "!" : "⚔"}
                            </span>
                            <div className="game-quest__copy">
                                <strong>{playerQuest.quest.name}</strong>
                                <span>{playerQuest.quest.description}</span>
                            </div>
                            <span className="game-quest__progress">{playerQuest.currentProgress}/{playerQuest.targetCount}</span>
                        </article>
                    ))}
                </div>
            </section>

            <nav className="game-side-menu" aria-label="Разделы игры">
                <img src="/assets/game/side-menu.png" alt="" aria-hidden="true" />
                <button type="button" aria-label="Инвентарь" onClick={() => navigate("/inventory")} />
                <button type="button" aria-label="Квесты" onClick={() => navigate("/quests")} />
                <button type="button" aria-label="Прокачка" onClick={() => navigate("/character")} />
                <button type="button" aria-label="Кузница" onClick={() => navigate("/forge")} />
                <button type="button" aria-label="Лор" onClick={() => navigate("/lore")} />
            </nav>

            <section className="game-actions" aria-label="Действия в локации">
                <img src="/assets/game/actions.png" alt="" aria-hidden="true" />
                <button type="button" aria-label="Путешествие" disabled={actionsDisabled || !allows("TRAVEL")} onClick={() => setTravelOpen(true)} />
                <button type="button" aria-label="Поиск" disabled={actionsDisabled || !allows("SEARCH")} onClick={() => runAction("search", explorationApi.search)} />
                <button type="button" aria-label="Охота" disabled={actionsDisabled || !allows("HUNT")} onClick={() => runAction("hunt", explorationApi.hunt)} />
            </section>

            <section className="game-rest" aria-label="Отдых">
                <img src="/assets/game/rest.png" alt="" aria-hidden="true" />
                <button type="button" aria-label="Короткий отдых" disabled={actionsDisabled || !allows("SHORT_REST")} onClick={() => handleRest("short")} />
                <button type="button" aria-label="Полный отдых" disabled={actionsDisabled || !allows("LONG_REST")} onClick={() => handleRest("long")} />
            </section>

            {message && <p className="game-dashboard__message" role="status">{message}</p>}

            {travelOpen && (
                <div className="game-modal" role="presentation" onMouseDown={() => setTravelOpen(false)}>
                    <section className="game-modal__dialog game-modal__dialog--travel" role="dialog" aria-modal="true" aria-labelledby="travel-title" onMouseDown={(event) => event.stopPropagation()}>
                        <img className="game-modal__frame" src="/assets/game/travel-panel.png" alt="" aria-hidden="true" />
                        <h2 id="travel-title" className="sr-only">Куда отправиться?</h2>
                        <div className="game-modal__destinations">
                            {location.connections.map((connection) => (
                                <button
                                    key={connection.id}
                                    type="button"
                                    className={`game-modal__destination${connection.open ? "" : " game-modal__destination--locked"}`}
                                    aria-label={`${connection.name}. Рекомендуемый уровень: ${connection.recommendedLevel}${connection.open ? "" : `. Путь закрыт: ${connection.blockedReasons.join(". ")}`}`}
                                    onClick={() => handleTravel(connection)}
                                >
                                    <span className="game-modal__destination-copy">
                                        <strong>{connection.name}</strong>
                                        <span>Рекомендуемый уровень: {connection.recommendedLevel}</span>
                                        {!connection.open && <small>{connection.blockedReasons.join(". ")}</small>}
                                    </span>
                                    <img src={travelIconFor(connection)} alt="" aria-hidden="true" />
                                </button>
                            ))}
                        </div>
                        <button type="button" className="game-modal__close" aria-label="Закрыть окно выбора направления" onClick={() => setTravelOpen(false)} />
                    </section>
                </div>
            )}

            {combatOpen && (
                <section className="game-combat" aria-label="Бой">
                    {combatState ? (
                        <CombatPanel
                            state={combatState}
                            monsterName={enemyName}
                            isLoading={combatLoading}
                            onAction={handleCombatAction}
                        />
                    ) : (
                        <div><Loading text="Загрузка боя..." /></div>
                    )}
                    {combatState && combatState.status !== "ACTIVE" && (
                        <button type="button" onClick={finishCombat}>Вернуться к действиям</button>
                    )}
                </section>
            )}
        </main>
    );
}
