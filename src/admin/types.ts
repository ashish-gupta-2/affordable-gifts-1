export type OrderStatus =
  | "ORDER_RECEIVED"
  | "PAYMENT_CONFIRMED"
  | "PREPARING"
  | "PACKED"
  | "DISPATCHED"
  | "OUT_FOR_DELIVERY"
  | "DELIVERED"
  | "CANCELLED";

/** Shipping stages in the order they normally happen (from the PRD). */
export const ORDER_STATUSES: { value: OrderStatus; label: string }[] = [
  { value: "ORDER_RECEIVED", label: "Order received" },
  { value: "PAYMENT_CONFIRMED", label: "Payment confirmed" },
  { value: "PREPARING", label: "Preparing gift box" },
  { value: "PACKED", label: "Packed" },
  { value: "DISPATCHED", label: "Dispatched" },
  { value: "OUT_FOR_DELIVERY", label: "Out for delivery" },
  { value: "DELIVERED", label: "Delivered" },
  { value: "CANCELLED", label: "Cancelled" },
];

export interface OrderSummary {
  orderNumber: string;
  status: OrderStatus;
  paymentStatus: "PENDING" | "PAID";
  customerName: string;
  customerPhone: string;
  packagingName: string;
  quantity: number;
  total: number;
  createdAt: string;
}

export interface OrderDetail {
  order: {
    orderNumber: string;
    status: OrderStatus;
    paymentStatus: "PENDING" | "PAID";
    packagingName: string;
    packagingUnitPrice: number;
    quantity: number;
    items: {
      slot: number;
      productName: string;
      weight: number;
      unitPrice: number;
    }[];
    productsSubtotal: number;
    packagingTotal: number;
    deliveryCharge: number;
    total: number;
    totalWeightGrams: number;
    customerName: string;
    customerPhone: string;
    addressLine: string;
    createdAt: string;
  };
  customerEmail: string | null;
  deliveryInstructions: string | null;
}

export interface AdminProduct {
  id: string;
  name: string;
  category: "Dry Fruits" | "Snacks";
  emoji: string;
  active: boolean;
  displayOrder: number;
  packs: { weight: number; price: number }[];
}

export interface AdminPackaging {
  id: string;
  name: string;
  kind: "box" | "jar";
  description: string;
  price: number;
  slotCount: number;
  slotCapacity: number;
  active: boolean;
  displayOrder: number;
}

export interface AdminSettings {
  minOrderValue: number;
  localDeliveryCharge: number;
  indiaDeliveryCharge: number;
}
