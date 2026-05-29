export interface EmployeeResponse {
  id: number;
  email: string;
  phone: string | null;
  active: boolean;
  owner: boolean;
  createdAt: string;
  version: number;
}
