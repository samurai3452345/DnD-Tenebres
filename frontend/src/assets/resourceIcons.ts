// Shared resource artwork for inventory and combat loot. Legacy names remain aliases.
const asset = (file: string) => `/assets/resources/${file}.png`;
export const resourceIcons: Record<string, string> = {
    "Кровоцвет": asset("bloodflower"),
    "Кожа": asset("leather"),
    "Дрова": asset("wood"),
    "Древесина": asset("wood"),
    "Припасы": asset("supplies"),
    "Железная руда": asset("iron-ore"),
    "Железный слиток": asset("iron-ingot"),
    "Железо": asset("iron-ingot"),
    "Драконья чешуя": asset("dragon-scales"),
    "Мифриловая руда": asset("mithril-ore"),
    "Мифриловый слиток": asset("mithril-ingot"),
    "Мифрил": asset("mithril-ingot"),
    "Орихалковая руда": asset("orichalcum-ore"),
    "Орихалковый слиток": asset("orichalcum-ingot"),
    "Орихалк": asset("orichalcum-ingot"),
};
