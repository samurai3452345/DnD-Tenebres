export interface Item {
    id: number;
    name: string;
    description: string;
    type: string;
    quantity: number;
}

export interface Inventory {
    items: {
        id: number;
        template: { name: string; type: string; rarity: string };
        equipped: boolean;
        amount: number;
    }[];
}
