import type { CheckoutDetails, DeliveryQuote, PlacedOrder } from "../types";

/** The backend reports problems as { detail: "..." }; show that text when present. */
async function errorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (typeof body.detail === "string") return body.detail;
  } catch {
    // not JSON; fall through
  }
  return `Request failed (${response.status})`;
}

export async function fetchDeliveryQuote(
  pincode: string
): Promise<DeliveryQuote> {
  const response = await fetch(
    `/api/delivery-quote?pincode=${encodeURIComponent(pincode)}`
  );
  if (!response.ok) throw new Error(await errorMessage(response));
  return response.json() as Promise<DeliveryQuote>;
}

export interface PlaceOrderInput {
  /** Same id on every retry of one checkout, so a double submit is harmless. */
  requestId: string;
  packagingId: string;
  quantity: number;
  /** One product id per slot, in slot order. Prices are never sent. */
  productIds: string[];
  details: CheckoutDetails;
}

export async function placeOrder(input: PlaceOrderInput): Promise<PlacedOrder> {
  const { details } = input;
  const response = await fetch("/api/orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      requestId: input.requestId,
      packagingId: input.packagingId,
      quantity: input.quantity,
      productIds: input.productIds,
      customer: {
        name: details.name,
        phone: details.phone,
        email: details.email,
      },
      address: {
        house: details.house,
        street: details.street,
        area: details.area,
        city: details.city,
        state: details.state,
        pincode: details.pincode,
        instructions: details.instructions,
      },
    }),
  });
  if (!response.ok) throw new Error(await errorMessage(response));
  return response.json() as Promise<PlacedOrder>;
}
