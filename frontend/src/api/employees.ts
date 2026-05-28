import { apiFetch } from "./http";
import type { EmployeeResponse } from "../types/EmployeeResponse";

export interface CreateEmployeeRequest {
  email: string;
  password: string;
  phone?: string;
}

export interface UpdateEmployeeRequest {
  email: string;
  phone?: string;
  active: boolean;
  password?: string;
}

export async function getEmployees(token: string): Promise<EmployeeResponse[]> {
  const res = await apiFetch(`/api/employees`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) {
    const err = await res.json().catch(() => null);
    throw new Error(err?.message ?? "Failed to load employees");
  }
  return res.json();
}

export async function createEmployee(
  payload: CreateEmployeeRequest,
  token: string,
): Promise<EmployeeResponse> {
  const res = await apiFetch(`/api/employees`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => null);
    throw new Error(err?.message ?? "Failed to create employee");
  }
  return res.json();
}

export async function updateEmployee(
  id: number,
  payload: UpdateEmployeeRequest,
  token: string,
): Promise<EmployeeResponse> {
  const res = await apiFetch(`/api/employees/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => null);
    throw new Error(err?.message ?? "Failed to update employee");
  }
  return res.json();
}
