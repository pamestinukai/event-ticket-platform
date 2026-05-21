import { API_BASE_URL } from "../constants";
import type { TicketTypeResponse } from "../types/TicketType";

export async function getTicketTypes(eventId: string | number, token: string): Promise<TicketTypeResponse[]> {
    const res = await fetch(`${API_BASE_URL}/api/events/${eventId}/ticket-types`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.message ?? "Failed to load ticket types");
    }
    return res.json();
}
