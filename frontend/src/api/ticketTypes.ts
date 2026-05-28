import { apiFetch } from './http';
import type { TicketTypeResponse } from '../types/TicketType';

export async function getTicketTypes(
    eventId: string | number,
    token?: string,
): Promise<TicketTypeResponse[]> {
    const headers: Record<string, string> = {};
    if (token) headers.Authorization = `Bearer ${token}`;
    const res = await apiFetch(`/api/events/${eventId}/ticket-types`, {
        headers,
    });
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.message ?? 'Failed to load ticket types');
    }
    return res.json();
}
