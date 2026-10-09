import { Fragment, useEffect, useRef } from "react";
import type { CombatEvent } from "../../types/combat";
import { CombatSymbol } from "./CombatSymbols";

export default function CombatLog({ events }: { events: CombatEvent[] }) {
    const scrollRef = useRef<HTMLDivElement>(null);
    useEffect(() => { const log = scrollRef.current; if (log) log.scrollTop = log.scrollHeight; }, [events]);
    return <aside className="combat-journal" aria-label="Журнал боя">
        <h2 className="sr-only">Журнал боя</h2>
        <div className="combat-journal__entries" ref={scrollRef} role="log" aria-live="polite" aria-relevant="additions">
            {events.length === 0 && <p className="combat-empty">Бой начинается…</p>}
            {events.map((event, index) => {
                const tone = /HEAL|BUFF|RESTORE/.test(event.actionType) ? "heal" : /MISS|FLEE/.test(event.actionType) ? "neutral"
                    : /CRIT/.test(event.actionType) ? "critical" : event.value > 0 || /DEATH|DAMAGE/.test(event.actionType) ? "damage" : "neutral";
                const startsRound = event.round != null && event.round > 0
                    && (index === 0 || events[index - 1].round !== event.round);
                return <Fragment key={index}>
                    {startsRound && <h3 className="combat-journal__round">Раунд {event.round}</h3>}
                    <article className={`combat-event combat-event--${tone}`}>
                    <CombatSymbol kind={event.actionType} /><div>
                        {event.actor !== "SYSTEM" && <strong>{event.actor}</strong>}<p>{event.description}</p>
                        {event.target && event.target !== event.actor && event.actor !== "SYSTEM" && <small>Цель: {event.target}</small>}
                        {event.value > 0 && <p className="combat-event__value">
                            {/HEAL/.test(event.actionType) ? "Восстановлено здоровья: "
                                : /MANA|RESTORE_MP/.test(event.actionType) ? "Восстановлено маны: "
                                : /ATTACK|SPELL|DAMAGE|CRIT|HIT/.test(event.actionType) ? "Урон: " : ""}{event.value}
                        </p>}
                    </div>
                    </article>
                </Fragment>;
            })}
        </div>
    </aside>;
}
