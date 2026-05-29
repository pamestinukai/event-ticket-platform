import { API_BASE_URL } from "../constants";

/**
 * Wrapper around fetch for all API calls. Prepends the API base URL and, when the
 * backend rejects our token (HTTP 401), clears the stored session and sends the user
 * to the login page. Callers keep handling res.ok / parsing as before.
 */
export async function apiFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const res = await fetch(`${API_BASE_URL}${path}`, options);
  if (res.status === 401) {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    if (window.location.pathname !== "/auth") {
      window.location.assign("/auth");
    }
  }
  return res;
}
