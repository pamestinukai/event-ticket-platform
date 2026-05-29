import { apiFetch } from "./http";
import type { OrganizationResponse } from "../types/OrganizationResponse";

export async function getMyOrganization(token: string): Promise<OrganizationResponse> {
    const res = await apiFetch(`/api/organizations/me`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) throw new Error("Failed to load organization");
    return res.json();
}
