import { API_BASE_URL } from "../constants";
import type {EventResponse} from "../types/EventResponse.ts";

export async function getAvailableEvents(): Promise<EventResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/events/public/available`, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
    });
    if (!response.ok) {
        await response.json().catch(() => null);
        switch (response.status) {
            case 401: throw new Error("Unauthorized");
            case 403: throw new Error("Forbidden");
            default: throw new Error("Failed to fetch events");
        }
    }
    return await response.json();
}

export async function getEventById(id: string): Promise<EventResponse> {
    const response = await fetch(`${API_BASE_URL}/api/events/public/${id}`, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
    })
    if (!response.ok) {
        await response.json();
        switch (response.status) {
            case 401: throw new Error("Unauthorized");
            case 403: throw new Error("Forbidden");
            case 404: throw new Error("Event not found");
            default: throw new Error("Failed to fetch event");
        }
    }
    return await response.json();
}