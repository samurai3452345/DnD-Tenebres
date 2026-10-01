export interface Item {
    id: number;
    name: string;
    description: string;
    type: string;
    quantity: number;
}

export interface Inventory {
    items: Item[];
}
