import { PotionSymbol } from "./CombatSymbols";
import "./potion-artwork.css";

export const potionArtwork = new Map<string, string>([
    ["Малое зелье лечения", "healing-small.png"],
    ["Зелье лечения", "healing.png"],
    ["Королевское зелье лечения", "healing-royal.png"],
    ["Аурелиев эликсир", "aurelius-elixir.png"],
    ["Амброзия", "ambrosia.png"],
    ["Малое зелье маны", "mana-small.png"],
    ["Зелье маны", "mana.png"],
    ["Королевское зелье маны", "mana-royal.png"],
]);

export default function CombatPotionArtwork({ name, action }: { name: string; action: string }) {
    const file = potionArtwork.get(name);
    return <span className="combat-potion-artwork" aria-hidden="true">
        <svg className="combat-potion-artwork__frame" viewBox="108 108 1038 1008" preserveAspectRatio="none">
            <image href="/assets/combat/potions/frame.png" width="1254" height="1254" />
        </svg>
        {file ? <img className="combat-potion-artwork__bottle" src={`/assets/combat/potions/${file}`} alt="" draggable={false} />
            : <span className="combat-potion-artwork__fallback"><PotionSymbol kind={action} /></span>}
    </span>;
}
