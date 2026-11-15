export interface PackPrice {
  /** Pack size in grams (e.g. 100 or 200). */
  weight: number;
  price: number;
}

export interface Product {
  id: string;
  name: string;
  category: "Dry Fruits" | "Snacks";
  emoji: string;
  packs: PackPrice[];
}

export interface Packaging {
  id: string;
  name: string;
  kind: "box" | "jar";
  description: string;
  /** Total packaging cost (box, or jars + outer cover). */
  price: number;
  /** Number of partitions (box) or jars (jar set). */
  slotCount: number;
  /** Grams of a single product each slot holds. */
  slotCapacity: number;
}

export interface GreetingCard {
  id: string;
  name: string;
  price: number;
}

export interface DeliveryQuote {
  pincode: string;
  zone: "local" | "india";
  charge: number;
}

/** What the customer types at checkout. */
export interface CheckoutDetails {
  name: string;
  phone: string;
  email: string;
  house: string;
  street: string;
  area: string;
  city: string;
  state: string;
  pincode: string;
  instructions: string;
}

export interface PlacedOrder {
  orderNumber: string;
  status: string;
  paymentStatus: string;
  packagingName: string;
  quantity: number;
  items: { slot: number; productName: string; weight: number; unitPrice: number }[];
  productsSubtotal: number;
  packagingTotal: number;
  deliveryCharge: number;
  total: number;
  totalWeightGrams: number;
  customerName: string;
  customerPhone: string;
  addressLine: string;
}
