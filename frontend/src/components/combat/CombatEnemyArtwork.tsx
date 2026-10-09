import type { CombatEnemyState } from "../../types/combat";
import "./enemy-artwork.css";

// Keys match backend avatarKey = "monster:" + monster template name.
export const enemyArtwork = new Map<string, string>([
    ["monster:Гоблин", "goblin.png"],
    ["monster:Гоблин-воин", "goblin-warrior.png"],
    ["monster:Гоблин-лучник", "goblin-archer.png"],
    ["monster:Гоблин-шаман-огня", "goblin-fire-shaman.png"],
    ["monster:Волк", "wolf.png"],
    ["monster:Варг", "warg.png"],
    ["monster:Бандит", "bandit.png"],
    ["monster:Скелет", "skeleton.png"],
    ["monster:Скелет-воин", "skeleton-warrior.png"],
    ["monster:Скелет-лучник", "skeleton-archer.png"],
    ["monster:Скелет-страж", "skeleton-guard.png"],
]);

export default function CombatEnemyArtwork({ enemy }: { enemy: CombatEnemyState | null }) {
    const file = enemy && enemyArtwork.get(enemy.avatarKey);
    return <div className="combat-stage__enemy" aria-hidden="true" data-avatar-key={enemy?.avatarKey}>
        {file && <img key={enemy?.avatarKey} className="combat-enemy-artwork"
            src={`/assets/combat/enemies/${file}`} alt="" draggable={false} />}
    </div>;
}
