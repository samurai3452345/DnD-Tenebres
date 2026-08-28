import { api } from "./axios";
import type { Inventory } from "../types/inventory";

export const inventoryApi = {

    getInventory: async (): Promise<Inventory> => {
        const response = await api.get<Inventory>(
            "/inventory"
        );

        return response.data;
    },

};
