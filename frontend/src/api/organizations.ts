import { API_BASE_URL } from "../constants";
import type { OrganizationResponse } from "../types/OrganizationResponse";

export async function getMyOrganization(token: string): Promise<OrganizationResponse> {
    const res = await fetch(`${API_BASE_URL}/api/organizations/me`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) throw new Error("Failed to load organization");
    return res.json();
}
