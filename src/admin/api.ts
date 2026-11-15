/** Thrown when the backend rejects the admin key, so the UI can log out. */
export class UnauthorizedError extends Error {
  constructor() {
    super("Invalid admin key");
  }
}

export interface AdminApi {
  get: <T>(path: string) => Promise<T>;
  send: <T>(method: "POST" | "PUT" | "PATCH" | "DELETE", path: string, body?: unknown) => Promise<T>;
}

async function readError(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (typeof body.detail === "string") return body.detail;
  } catch {
    // not JSON; fall through
  }
  return `Request failed (${response.status})`;
}

/** Every call carries the admin key; a 401 calls `onUnauthorized` and throws. */
export function createAdminApi(
  key: string,
  onUnauthorized: () => void
): AdminApi {
  async function request<T>(
    method: string,
    path: string,
    body?: unknown
  ): Promise<T> {
    const response = await fetch(`/api/admin${path}`, {
      method,
      headers: {
        "X-Admin-Key": key,
        ...(body !== undefined && { "Content-Type": "application/json" }),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });

    if (response.status === 401) {
      onUnauthorized();
      throw new UnauthorizedError();
    }
    if (!response.ok) throw new Error(await readError(response));
    if (response.status === 204) return undefined as T;
    return response.json() as Promise<T>;
  }

  return {
    get: (path) => request("GET", path),
    send: (method, path, body) => request(method, path, body),
  };
}

/** Checks a key by calling a cheap admin endpoint. */
export async function verifyAdminKey(key: string): Promise<boolean> {
  const response = await fetch("/api/admin/settings", {
    headers: { "X-Admin-Key": key },
  });
  if (response.status === 401) return false;
  if (!response.ok) throw new Error(await readError(response));
  return true;
}
