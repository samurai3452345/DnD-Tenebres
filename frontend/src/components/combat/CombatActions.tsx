import { useEffect, useRef, useState } from "react";
import type { CombatActionRequest, CombatState, CombatAbilityState, CombatPotionState } from "../../types/combat";
import { CombatBar, CombatEffects, CombatSymbol, elementNames } from "./CombatSymbols";
import CombatPotionArtwork from "./CombatPotionArtwork";

type Selection = { kind: "spell"; item: CombatAbilityState } | { kind: "potion"; item: CombatPotionState };
function unavailableReason(reason: string | null) {
    if (reason === "MAGIC_FOCUS_REQUIRED") return "Нужно подходящее магическое оружие";
    if (reason === "NOT_ENOUGH_MANA") return "Не хватает маны";
    return "Недоступно";
}
export default function CombatActions({ state, onAction, disabled }: {
    state: CombatState; onAction: (request: CombatActionRequest) => void; disabled: boolean;
}) {
    const [selection, setSelection] = useState<Selection | null>(null);
    const confirmRef = useRef<HTMLButtonElement>(null);
    const allows = (action: CombatActionRequest["action"]) => !disabled && state.allowedActions.includes(action);
    useEffect(() => { setSelection(null); }, [state.encounterId, state.round, state.status]);
    useEffect(() => {
        if (!selection) return;
        const previousFocus = document.activeElement;
        confirmRef.current?.focus();
        const escape = (event: KeyboardEvent) => {
            if (event.key === "Escape") setSelection(null);
            if (event.key === "Tab") {
                const buttons = confirmRef.current?.parentElement?.querySelectorAll<HTMLButtonElement>("button");
                if (buttons?.length === 2) { event.preventDefault(); (document.activeElement === buttons[0] ? buttons[1] : buttons[0]).focus(); }
            }
        };
        document.addEventListener("keydown", escape);
        return () => { document.removeEventListener("keydown", escape); if (previousFocus instanceof HTMLElement) previousFocus.focus(); };
    }, [selection]);
    const send = (action: CombatActionRequest["action"], abilityId: number | null = null, itemId: number | null = null) => {
        setSelection(null);
        onAction({ action, abilityId, itemId, targetId: action === "ATTACK" || action === "CAST_SPELL" ? state.currentEnemy?.id ?? null : null });
    };
    const selectedAvailable = selection?.kind === "spell"
        ? allows("CAST_SPELL") && !!state.currentEnemy && state.availableAbilities.some(a => a.id === selection.item.id && a.available)
        : selection?.kind === "potion" && allows("USE_POTION") && state.availablePotions.some(p => p.itemId === selection.item.itemId && p.available && p.amount > 0);
    return <>
        <section className="combat-dock" aria-label="Персонаж и способности">
            <section className="combat-hero" aria-label="Герой">
                <div className="combat-hero__interface">
                <h2>{state.player.name}</h2>
                <div className="combat-hero__vitals">
                <CombatBar kind="hp" value={state.player.currentHp} maximum={state.player.maxHp} />
                <CombatBar kind="mp" value={state.player.currentMp} maximum={state.player.maxMp} />
                <CombatEffects effects={state.player.effects} />
                </div>
                </div>
            </section>
            <section className="combat-spells" aria-label="Заклинания"><h2 className="sr-only">Заклинания</h2><div className="combat-cards">
                {state.availableAbilities.map(ability => <button key={ability.id} type="button" className="combat-card"
                    disabled={!allows("CAST_SPELL") || !ability.available || !state.currentEnemy}
                    title={ability.available ? `${ability.name}, ${elementNames[ability.element] ?? ability.element}` : unavailableReason(ability.unavailableReason)}
                    onClick={() => setSelection({ kind: "spell", item: ability })}>
                    <span className="combat-card__icon"><CombatSymbol kind={ability.element} /></span>
                    <strong>{ability.name}</strong><span>Мана: {ability.manaCost}</span>
                    {!ability.available && <small>{ability.unavailableReason === "MAGIC_FOCUS_REQUIRED" ? "Нужно маг. оружие" : unavailableReason(ability.unavailableReason)}</small>}
                </button>)}
                {!state.availableAbilities.length && <p className="combat-empty">Заклинаний пока нет</p>}
            </div></section>
            <section className="combat-potions" aria-label="Зелья"><h2 className="sr-only">Зелья</h2><div className="combat-cards">
                {state.availablePotions.map(potion => <button key={potion.itemId} type="button" className="combat-card"
                    disabled={!allows("USE_POTION") || !potion.available || potion.amount <= 0}
                    onClick={() => setSelection({ kind: "potion", item: potion })}>
                    <CombatPotionArtwork name={potion.name} action={potion.action} />
                    <strong>{potion.name}</strong><span>×{potion.amount}</span>
                </button>)}
                {!state.availablePotions.length && <p className="combat-empty">Нет зелий</p>}
            </div></section>
        </section>
        <div className="combat-main-actions" aria-label="Боевые действия">
            <button className="combat-image-button" type="button" disabled={!allows("ATTACK") || !state.currentEnemy}
                aria-label="Атаковать" onClick={() => send("ATTACK")}><img src="/assets/combat/attack.png" alt="Атаковать" /></button>
            <button className="combat-image-button" type="button" disabled={!allows("FLEE")}
                aria-label="Сбежать" onClick={() => send("FLEE")}><img src="/assets/combat/flee.png" alt="Сбежать" /></button>
        </div>
        {selection && <div className="combat-confirm-backdrop" onMouseDown={() => setSelection(null)}>
            <section className="combat-confirm" role="dialog" aria-modal="true" aria-labelledby="combat-confirm-title" onMouseDown={event => event.stopPropagation()}>
                {selection.kind === "spell" ? <CombatSymbol kind={selection.item.element} />
                    : <CombatPotionArtwork name={selection.item.name} action={selection.item.action} />}
                <h2 id="combat-confirm-title">{selection.item.name}</h2>
                <p>{selection.kind === "spell" ? `Использовать заклинание? Расход маны: ${selection.item.manaCost}.` : `Использовать зелье? В наличии: ${selection.item.amount}.`}</p>
                <div><button ref={confirmRef} type="button" disabled={!selectedAvailable} onClick={() => selection.kind === "spell"
                    ? send("CAST_SPELL", selection.item.id) : send("USE_POTION", null, selection.item.itemId)}>Использовать</button>
                    <button type="button" onClick={() => setSelection(null)}>Отмена</button></div>
            </section>
        </div>}
    </>;
}
