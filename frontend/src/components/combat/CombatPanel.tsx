import { useEffect, useRef, useState } from "react";
import type { CombatActionRequest, CombatState } from "../../types/combat";
import CombatLog from "./CombatLog";
import CombatActions from "./CombatActions";
import EnemyHealthBar from "./EnemyHealthBar";
import { CombatEffects, CombatSymbol, elementNames } from "./CombatSymbols";
import "./combat.css";

interface CombatPanelProps {
    state: CombatState; monsterName: string; isLoading: boolean; onAction: (request: CombatActionRequest) => void;
    onFinish: () => void; background: string; error: string | null;
}
const statusLabels: Partial<Record<CombatState["status"], string>> = {
    VICTORY: "Победа!", DEFEAT: "Поражение", FLED: "Побег удался", CANCELLED: "Бой прерван",
};
export default function CombatPanel({ state, monsterName, isLoading, onAction, onFinish, background, error }: CombatPanelProps) {
    const [lastEnemy, setLastEnemy] = useState(state.currentEnemy);
    const screenRef = useRef<HTMLElement>(null);
    useEffect(() => {
        const previousFocus = document.activeElement;
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        screenRef.current?.focus();
        return () => {
            document.body.style.overflow = previousOverflow;
            if (previousFocus instanceof HTMLElement) previousFocus.focus();
        };
    }, []);
    useEffect(() => { if (state.currentEnemy) setLastEnemy(state.currentEnemy); }, [state.currentEnemy]);
    const enemy = state.currentEnemy ?? lastEnemy;
    const isFinished = state.status !== "ACTIVE";
    return <section ref={screenRef} tabIndex={-1} className="combat-screen" style={{ backgroundImage: `url("${background}")` }} aria-label="Поле боя" aria-busy={isLoading}>
        <div className="combat-screen__shade" aria-hidden="true" />
        <div className="combat-stage">
            <header className="combat-enemy" aria-label="Противник">
                <img className="combat-enemy__frame" src="/assets/combat/enemy-panel-no-hp.png" alt="" />
                <h1>{monsterName}</h1><span className="combat-enemy__level">{enemy?.level ?? "—"}</span>
                {enemy?.elements.find(element => element !== "PHYSICAL") && <span className="combat-enemy__element-icon"><CombatSymbol kind={enemy.elements.find(element => element !== "PHYSICAL")!} /></span>}
                <span className="combat-enemy__elements">{enemy?.elements.filter(element => element !== "PHYSICAL").map(element => elementNames[element] ?? element).join(", ") || "Нет стихии"}</span>
                <div className="combat-enemy__effects"><CombatEffects effects={state.currentEnemy?.effects ?? []} /></div>
                <EnemyHealthBar value={state.currentEnemy?.currentHp ?? (state.status === "VICTORY" ? 0 : enemy?.currentHp ?? 0)} maximum={enemy?.maxHp ?? 0} />
            </header>
            {state.remainingEnemies > 1 && <div className="combat-stage__caption"><span>Противников: {state.remainingEnemies}</span></div>}
            {/* Enemy artwork will be mapped to the backend avatarKey when supplied. */}
            <div className="combat-stage__enemy" aria-hidden="true" data-avatar-key={enemy?.avatarKey} />
            {isFinished && <div className="combat-result" role="status"><h2>{statusLabels[state.status]}</h2>
                <p>{state.status === "VICTORY" ? "Противник повержен. Награды — в журнале боя." : "Бой окончен."}</p>
                <button type="button" onClick={onFinish}>Вернуться в локацию</button></div>}
            {isLoading && <p className="combat-screen__notice" role="status">Выполняется действие…</p>}
            {error && <p className="combat-screen__notice combat-screen__notice--error" role="alert">{error}</p>}
        </div>
        <CombatLog events={state.journal} />
        <CombatActions state={state} onAction={onAction} disabled={isFinished || isLoading} />
    </section>;
}
