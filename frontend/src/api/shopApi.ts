import { api } from "./axios";

export interface BuyRequest {
    operationId: string;
    offerId: number;
    amount: number;
}

export interface SellRequest {
    operationId: string;
    playerItemId: number;
    amount: number;
}

export interface ShopOffer {
    offerId: number;
    templateId: number;
    name: string;
    type: string;
    rarity: string;
    unitPrice: number;
    availableQuantity: number;
    minLevel: number;
    available: boolean;
}

export interface TradeResult {
    operationId: string;
    operationType: 'BUY' | 'SELL';
    resourceId: number;
    amount: number;
    message: string;
    alreadyProcessed: boolean;
}

export const shopApi = {
    getOffers: async (): Promise<ShopOffer[]> => {
        const response = await api.get<ShopOffer[]>("/merchants/current/offers");
        return response.data;
    },

    buyItem: async (request: BuyRequest): Promise<TradeResult> => {
        const response = await api.post<TradeResult>(
            "/merchants/current/buy",
            request
        );
        return response.data;
    },

    sellItem: async (request: SellRequest): Promise<TradeResult> => {
        const response = await api.post<TradeResult>(
            "/merchants/current/sell",
            request
        );
        return response.data;
    }
};
