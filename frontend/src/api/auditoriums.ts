import { apiFetch } from "./http";
import type { AuditoriumResponse } from "../types/AuditoriumResponse";

export async function getAuditoriums(token: string): Promise<AuditoriumResponse[]> {
    const res = await apiFetch(`/api/auditoriums`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) throw new Error("Failed to load auditoriums");
    return res.json();
}
