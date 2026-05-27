import { API_BASE_URL } from '../constants';
import type {
    TicketReservationRequest,
    TicketReservationResponse,
} from '../types/TicketReservation';

export async function reserveTickets(
    payload: TicketReservationRequest,
): Promise<TicketReservationResponse> {
    const res = await fetch(`${API_BASE_URL}/api/tickets/reserve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.message ?? 'Failed to reserve tickets');
    }
    return res.json();
}

export async function cancelReservation(purchaseId: number): Promise<void> {
    const res = await fetch(
        `${API_BASE_URL}/api/tickets/reserve/${purchaseId}/cancel`,
        { method: 'POST' },
    );
    if (!res.ok) throw new Error('Failed to cancel reservation');
}

export async function confirmReservation(purchaseId: number): Promise<void> {
    const res = await fetch(
        `${API_BASE_URL}/api/tickets/reserve/${purchaseId}/confirm`,
        { method: 'POST' },
    );
    if (!res.ok) throw new Error('Failed to confirm reservation');
}
