import { API_BASE_URL } from '../constants';

export interface CreateCheckoutSessionRequest {
    purchaseId: number;
    buyerName: string;
    buyerEmail: string;
}

export interface CreateCheckoutSessionResponse {
    checkoutUrl: string;
}

export interface PurchaseSummaryResponse {
    purchaseId: number;
    buyerName: string;
    buyerEmail: string;
    totalPrice: number;
    currency: string;
    status: string;
    eventId: number;
    eventName: string;
    eventStartDatetime: string;
    eventEndDatetime: string;
    venueName: string;
    venueCity: string;
    auditoriumName: string;
    eventImages: string[];
    tickets: Array<{
        ticketTypeId: number;
        ticketTypeName: string;
        quantity: number;
        pricePerTicket: number;
    }>;
}

export async function createCheckoutSession(
    payload: CreateCheckoutSessionRequest,
): Promise<CreateCheckoutSessionResponse> {
    const res = await fetch(`${API_BASE_URL}/api/payment/checkout-session`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.message ?? 'Failed to create checkout session');
    }
    return res.json();
}

export async function getPurchaseSummary(purchaseId: number): Promise<PurchaseSummaryResponse> {
    const res = await fetch(`${API_BASE_URL}/api/payment/purchase/${purchaseId}/summary`);
    if (!res.ok) throw new Error('Failed to load purchase summary');
    return res.json();
}
