import { API_BASE_URL } from "../constants";
import type { VenueResponse } from "../types/VenueResponse";

export async function getVenues(token: string): Promise<VenueResponse[]> {
    const res = await fetch(`${API_BASE_URL}/api/venues`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) throw new Error("Failed to load venues");
    return res.json();
}
