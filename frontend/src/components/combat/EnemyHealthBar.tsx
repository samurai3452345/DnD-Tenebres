import type { CSSProperties } from "react";

// View boxes exclude only the transparent margins when rendering; source PNGs remain unchanged.
const art = {
    frame: { width: 2048, height: 684, viewBox: "0 208 2048 252" },
    20: { width: 2172, height: 724, viewBox: "104 296 548 112" },
    40: { width: 2172, height: 724, viewBox: "92 300 904 124" },
    60: { width: 2172, height: 724, viewBox: "84 300 1276 116" },
    80: { width: 2172, height: 724, viewBox: "108 300 1568 124" },
    100: { width: 2172, height: 724, viewBox: "100 292 1972 132" },
};
const levels = [20, 40, 60, 80, 100] as const;

function HealthArtwork({ level }: { level: "frame" | typeof levels[number] }) {
    const source = art[level];
    return <svg viewBox={source.viewBox} preserveAspectRatio="none" aria-hidden="true">
        <image href={`/assets/combat/hp-${level === "frame" ? "frame" : `fill-${level}`}.png`}
            width={source.width} height={source.height} />
    </svg>;
}

export default function EnemyHealthBar({ value, maximum }: { value: number; maximum: number }) {
    const safeMaximum = Math.max(0, maximum);
    const safeValue = Math.max(0, Math.min(safeMaximum, value));
    const percent = safeMaximum > 0 ? safeValue / safeMaximum * 100 : 0;
    const activeLevel = levels.find(level => percent <= level) ?? 100;
    return <div className="combat-enemy-hp" role="meter" aria-label="Здоровье противника"
        aria-valuenow={safeValue} aria-valuemin={0} aria-valuemax={safeMaximum}>
        <div className="combat-enemy-hp__frame"><HealthArtwork level="frame" /></div>
        {levels.map(level => <div key={level} className={`combat-enemy-hp__fill combat-enemy-hp__fill--${level}`}
            data-active={percent > 0 && activeLevel === level}
            style={{ "--hp-visible": `${Math.min(100, percent / level * 100)}%` } as CSSProperties}>
            <HealthArtwork level={level} />
        </div>)}
        <span className="combat-enemy-hp__value">{safeValue} / {safeMaximum}</span>
    </div>;
}
