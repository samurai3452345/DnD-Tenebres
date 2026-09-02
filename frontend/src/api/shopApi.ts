import { api } from "./axios";

export interface BuyRequest {
    templateName: string;
    amount: number;
}

export interface SellRequest {
    playerItemId: number;
    amount: number;
}

export interface ShopResponse {
    status: string;
    message: string;
}

export const shopApi = {
    buyItem: async (request: BuyRequest): Promise<ShopResponse> => {
        const response = await api.post<ShopResponse>(
            "/merchant/buy",
            request
        );
        return response.data;
    },

    sellItem: async (request: SellRequest): Promise<ShopResponse> => {
        const response = await api.post<ShopResponse>(
            "/merchant/sell",
            request
        );
        return response.data;
    }
};