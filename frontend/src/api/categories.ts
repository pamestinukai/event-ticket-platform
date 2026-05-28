import { apiFetch } from './http';
import type { CategoryResponse } from '../types/CategoryResponse';

export async function getPublicCategories(): Promise<
  CategoryResponse[]
> {
  const res = await apiFetch(`/api/categories`);
  if (!res.ok) throw new Error('Failed to load categories');
  return res.json();
}

export async function getCategories(
  token: string,
): Promise<CategoryResponse[]> {
  const res = await apiFetch(`/api/categories`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error('Failed to load categories');
  return res.json();
}
