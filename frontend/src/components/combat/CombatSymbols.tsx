import type { CombatEffect } from "../../types/combat";

export const elementNames: Record<string, string> = {
    PHYSICAL: "Нет стихии", FIRE: "Огонь", WATER: "Вода", EARTH: "Земля", AIR: "Воздух",
    NATURE: "Природа", ICE: "Лёд", ELECTRICITY: "Молния", LIGHT: "Свет", DARK: "Тьма",
};
const effectNames: Record<string, string> = {
    BLEED: "Кровотечение", BLEEDING: "Кровотечение", POISON: "Яд", BURN: "Горение", BURNING: "Горение",
    STUN: "Оглушение", FREEZE: "Заморозка", REGENERATION: "Регенерация", WEAKNESS: "Слабость",
    SHIELD: "Щит", DEFENSE_UP: "Защита", ATTACK_UP: "Усиление атаки",
    PROTECTION_REDUCED: "Снижение защиты", MAGIC_SICKNESS: "Магическая болезнь", FROSTBITE: "Обморожение",
    BLIND: "Ослепление", SUPPRESSION: "Подавление", SHOCK: "Шок", DAMAGE_REDUCTION: "Снижение урона",
    THORNS: "Шипы", PROTECTION_UP: "Повышение защиты", TREATMENT: "Лечение", DAMAGE_UP: "Повышение урона",
    SHADOW_DEATH: "Теневая смерть", WELL_RESTED: "Хороший отдых", ABSOLUTE_SHIELD: "Абсолютный щит",
    LIGHT_MARK: "Метка света", LIFESTEAL: "Вампиризм", SHIELD_HP: "Поглощение урона",
    BERSERKER_RAGE: "Ярость берсерка", BLOOD_CONTRACT: "Контракт крови", GOLEM: "Голем", NONE: "Нет эффекта",
};
export function CombatSymbol({ kind }: { kind: string }) {
    const key = kind.toUpperCase();
    const glyph = /HEAL|REGEN/.test(key) ? "✚" : /MANA|RESTORE_MP/.test(key) ? "◆"
        : /BLEED/.test(key) ? "♦" : /POISON|DARK|WEAK/.test(key) ? "☽"
        : /SHIELD|DEFENSE/.test(key) ? "⬡" : /ICE|FREEZE/.test(key) ? "❄"
        : /FIRE|BURN/.test(key) ? "♨" : /LIGHT|ELECTRIC/.test(key) ? "ϟ"
        : /NATURE|EARTH/.test(key) ? "❧" : /WATER/.test(key) ? "◈" : /AIR/.test(key) ? "≋" : "⚔";
    return <span aria-hidden="true" className={`combat-symbol combat-symbol--${key.toLowerCase()}`}>{glyph}</span>;
}
export function PotionSymbol({ kind }: { kind: string }) {
    const color = /MP|MANA/.test(kind) ? "#2189ff" : /REGEN/.test(kind) ? "#42d88a" : "#e21c32";
    return <svg className="combat-potion-symbol" viewBox="0 0 64 80" aria-hidden="true">
        <path d="M25 9h14v19c0 5 15 8 15 23v14c0 6-7 9-22 9S10 71 10 65V51c0-15 15-18 15-23Z" fill="#b4d7ef22" stroke="#ddb474" strokeWidth="2" />
        <path d="M13 49h38v16c0 4-6 6-19 6s-19-2-19-6Z" fill={color} />
        <ellipse cx="32" cy="49" rx="19" ry="4" fill={color} stroke="#fff6" />
        <path d="M20 40c-4 4-5 10-5 18" stroke="#fff9" strokeWidth="3" fill="none" />
        <rect x="23" y="5" width="18" height="8" rx="2" fill="#ac7241" stroke="#ebc581" />
        <path d="M24 18h16" stroke="#f1d9ac" strokeWidth="3" />
    </svg>;
}
export function CombatEffects({ effects }: { effects: CombatEffect[] }) {
    return <div className="combat-effects" aria-label="Активные эффекты">
        {effects.map((effect, index) => <span className="combat-effect" key={`${effect.type}-${index}`}
            title={`${effectNames[effect.type] ?? effect.type}: ${effect.remainingRounds} раундов, сила ${effect.power}`}>
            <span className="sr-only">{effectNames[effect.type] ?? effect.type}, осталось раундов: {effect.remainingRounds}</span>
            <CombatSymbol kind={effect.type} /><span className="combat-effect__duration" aria-hidden="true">{effect.remainingRounds}</span>
        </span>)}
    </div>;
}
export function CombatBar({ value, maximum, kind }: { value: number; maximum: number; kind: "hp" | "mp" }) {
    const percent = maximum > 0 ? Math.max(0, Math.min(100, value / maximum * 100)) : 0;
    return <div className={`combat-bar combat-bar--${kind}`} role="meter" aria-label={kind === "hp" ? "Здоровье" : "Мана"}
        aria-valuenow={value} aria-valuemin={0} aria-valuemax={maximum}>
        <span className="combat-bar__fill" style={{ width: `${percent}%` }} /><span className="combat-bar__value">{value} / {maximum}</span>
    </div>;
}
