import { API_BASE_URL } from "../constants";
import type { CategoryResponse } from "../types/CategoryResponse";

export async function getCategories(token: string): Promise<CategoryResponse[]> {
    const res = await fetch(`${API_BASE_URL}/api/categories`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) throw new Error("Failed to load categories");
    return res.json();
}
