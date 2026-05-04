import { API_BASE_URL } from '../constants';

export async function getCount(): Promise<number> {
  const res = await fetch(`${API_BASE_URL}/api/count`);
  return res.json();
}

export async function incrementCount(): Promise<number> {
  const res = await fetch(`${API_BASE_URL}/api/count`, { method: 'POST' });
  return res.json();
}
