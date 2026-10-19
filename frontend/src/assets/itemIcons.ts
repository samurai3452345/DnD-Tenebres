import { resourceIcons } from "./resourceIcons";
import { weaponIcons } from "./weaponIcons";
import { armorIcons } from "./armorIcons";

export const itemIcons: Record<string, string> = { ...resourceIcons, ...weaponIcons, ...armorIcons };
