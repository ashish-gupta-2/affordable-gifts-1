import type { Packaging, Product } from "../types";

// The only place that knows where catalogue data comes from.
// Data is served by the backend (see backend/); in dev, Vite proxies /api.

async function getJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Request to ${url} failed with status ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export function fetchProducts(): Promise<Product[]> {
  return getJson<Product[]>("/api/products");
}

export function fetchPackagingOptions(): Promise<Packaging[]> {
  return getJson<Packaging[]>("/api/packaging");
}

export async function fetchMinOrderValue(): Promise<number> {
  const settings = await getJson<{ minOrderValue: number }>("/api/settings");
  return settings.minOrderValue;
}
