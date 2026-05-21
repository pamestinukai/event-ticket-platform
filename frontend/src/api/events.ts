import { API_BASE_URL } from "../constants";
import type {EventResponse} from "../types/EventResponse.ts";
import type { EventRequest } from "../types/EventRequest.ts";

export async function getEventById(id: string, token?: string): Promise<EventResponse> {
    const url = token
        ? `${API_BASE_URL}/api/events/${id}`
        : `${API_BASE_URL}/api/events/public/${id}`;
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (token) headers.Authorization = `Bearer ${token}`;
    const response = await fetch(url, { method: "GET", headers });
    if (!response.ok) {
        switch (response.status) {
            case 401: throw new Error("Unauthorized");
            case 403: throw new Error("Forbidden");
            case 404: throw new Error("Event not found");
            default: throw new Error("Failed to fetch event");
        }
    }
    return await response.json();
}

export async function searchPublicEvents(keyword: string): Promise<EventResponse[]> {
    const q = keyword.trim();
    const base = `${API_BASE_URL}/api/events/public/search`;
    const commonParams = `size=100&sortBy=startDatetime&sortDir=asc`;

    if (!q) {
        const res = await fetch(`${base}?${commonParams}`);
        if (!res.ok) throw new Error("Failed to load events");
        return (await res.json()).content;
    }

    const [kwRes, perfRes] = await Promise.all([
        fetch(`${base}?keyword=${encodeURIComponent(q)}&${commonParams}`),
        fetch(`${base}?performer=${encodeURIComponent(q)}&${commonParams}`),
    ]);
    if (!kwRes.ok || !perfRes.ok) throw new Error("Failed to search events");
    const [kwData, perfData] = await Promise.all([kwRes.json(), perfRes.json()]);

    const seen = new Set<string>();
    return [...kwData.content, ...perfData.content].filter(e => {
        const key = String(e.eventId);
        if (seen.has(key)) return false;
        seen.add(key);
        return true;
    });
}

export async function getMyEvents(token: string): Promise<EventResponse[]> {
    const res = await fetch(`${API_BASE_URL}/api/events/mine?sortBy=startDatetime&sortDir=desc&size=100`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.message ?? "Failed to load events");
    }
    const data = await res.json();
    return data.content;
}

export async function createEvent(payload: EventRequest, token: string): Promise<EventResponse> {
    const res = await fetch(`${API_BASE_URL}/api/events`, {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.message ?? "Failed to create event");
    }
    return res.json();
}

export async function updateEvent(id: string, payload: EventRequest, token: string): Promise<EventResponse> {
    const res = await fetch(`${API_BASE_URL}/api/events/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.message ?? "Failed to update event");
    }
    return res.json();
}
