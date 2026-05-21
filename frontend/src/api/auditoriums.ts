import { API_BASE_URL } from "../constants";
import type { AuditoriumResponse } from "../types/AuditoriumResponse";

export async function getAuditoriums(token: string): Promise<AuditoriumResponse[]> {
    const res = await fetch(`${API_BASE_URL}/api/auditoriums`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) throw new Error("Failed to load auditoriums");
    return res.json();
}
