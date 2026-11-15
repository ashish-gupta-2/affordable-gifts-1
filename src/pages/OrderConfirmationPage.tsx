import { formatRupees } from "../lib/format";
import type { PlacedOrder } from "../types";

interface Props {
  order: PlacedOrder;
  onNewOrder: () => void;
}

export default function OrderConfirmationPage({ order, onNewOrder }: Props) {
  return (
    <div className="mx-auto max-w-2xl rounded-3xl bg-white p-8 shadow-xl">
      <div className="text-center">
        <div className="text-6xl">🎉</div>
        <h2 className="mt-3 text-3xl font-bold text-green-700">
          Order placed!
        </h2>
        <p className="mt-2 text-gray-600">
          Your order number is{" "}
          <span className="font-bold text-gray-900">{order.orderNumber}</span>
        </p>
        <p className="mt-1 text-sm text-gray-500">
          Payment is pending. We will contact {order.customerName} on{" "}
          {order.customerPhone} to confirm.
        </p>
      </div>

      <hr className="my-6 border-gray-200" />

      <p className="font-semibold">
        {order.quantity} × {order.packagingName}
      </p>
      <ul className="mt-2 space-y-1 text-sm">
        {order.items.map((item) => (
          <li key={item.slot} className="flex justify-between">
            <span>
              {item.productName} · {item.weight}g
            </span>
            <span>{formatRupees(item.unitPrice)}</span>
          </li>
        ))}
      </ul>
      <p className="mt-1 text-xs text-gray-500">
        Prices above are for one set; every set is identical.
      </p>

      <dl className="mt-4 space-y-2">
        <div className="flex justify-between">
          <dt>Products</dt>
          <dd>{formatRupees(order.productsSubtotal)}</dd>
        </div>
        <div className="flex justify-between">
          <dt>Packaging</dt>
          <dd>{formatRupees(order.packagingTotal)}</dd>
        </div>
        <div className="flex justify-between">
          <dt>Delivery</dt>
          <dd>{formatRupees(order.deliveryCharge)}</dd>
        </div>
      </dl>

      <div className="mt-4 flex justify-between rounded-xl bg-green-50 p-4 text-xl">
        <span>Total</span>
        <span className="font-bold text-green-700">
          {formatRupees(order.total)}
        </span>
      </div>

      <p className="mt-6 text-sm text-gray-600">
        <span className="font-semibold">Delivering to:</span>{" "}
        {order.addressLine}
      </p>

      <button
        type="button"
        onClick={onNewOrder}
        className="mt-8 w-full rounded-xl bg-green-600 py-3 text-lg font-semibold text-white transition hover:bg-green-700"
      >
        Build another gift box
      </button>
    </div>
  );
}
