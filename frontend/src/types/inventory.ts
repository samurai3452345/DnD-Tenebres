export interface Item {
    id: number;
    name: string;
    description: string;
    type: string;
    quantity: number;
}

export interface Inventory {
    items: InventoryItem[];
    occupiedSlots: number;
}

export interface InventoryItem {
    id: number;
    template: { id: number; name: string; type: string; rarity: string };
    amount: number;
    equipped: boolean;
    locked: boolean;
    tier: number;
}
