import { useEffect, useRef } from "react";
import type { CombatReward } from "../../types/combat";
import "./victory.css";

// Add supplied artwork here, keyed by the backend iconKey (e.g. "item:Ржавый кинжал").
const rewardIcons: Record<string, string> = {};

function Asset({ file, viewBox, className }: { file: string; viewBox: string; className: string }) {
    return <svg className={className} viewBox={viewBox} preserveAspectRatio="none" aria-hidden="true">
        <image href={`/assets/combat/${file}`} width={file === "victory-loot-row.png" ? 2048 : 1254}
            height={file === "victory-loot-row.png" ? 682 : 1254} />
    </svg>;
}

export default function CombatVictory({ rewards, onFinish }: { rewards?: CombatReward[]; onFinish: () => void }) {
    const exitRef = useRef<HTMLButtonElement>(null);
    useEffect(() => {
        const previous = document.activeElement;
        exitRef.current?.focus();
        return () => { if (previous instanceof HTMLElement) previous.focus(); };
    }, []);
    return <div className="combat-victory-backdrop">
        <section className="combat-victory" role="dialog" aria-modal="true" aria-labelledby="combat-victory-title"
            onKeyDown={event => {
                if (event.key === "Tab") {
                    const loot = event.currentTarget.querySelector<HTMLElement>(".combat-victory__loot");
                    event.preventDefault();
                    if (document.activeElement === exitRef.current) loot?.focus();
                    else exitRef.current?.focus();
                }
            }}>
            <img className="combat-victory__frame" src="/assets/combat/victory-frame.png" alt="" />
            <h2 id="combat-victory-title" className="sr-only">Вы выиграли! Полученный лут</h2>
            <div className="combat-victory__loot" tabIndex={0} aria-label="Полученные награды">
                {rewards?.map(reward => <div className="combat-victory__item" key={reward.iconKey}>
                    <Asset className="combat-victory__row-frame" file="victory-loot-row.png" viewBox="70 178 1910 320" />
                    <div className="combat-victory__icon">
                        <Asset className="combat-victory__icon-frame" file="victory-item-frame.png" viewBox="98 98 1058 1032" />
                        {rewardIcons[reward.iconKey] && <img className="combat-victory__item-art" src={rewardIcons[reward.iconKey]} alt="" />}
                    </div>
                    <span className="combat-victory__name">{reward.name}</span>
                    <span className="combat-victory__amount">× {reward.amount}</span>
                </div>)}
                {!rewards?.length && <p className="combat-victory__empty">{rewards ? "Добычи нет." : "Награды доступны в журнале боя. Перезапусти backend для списка лута."}</p>}
            </div>
            <button ref={exitRef} type="button" className="combat-victory__exit" onClick={onFinish} aria-label="Выйти в локацию" />
        </section>
    </div>;
}
