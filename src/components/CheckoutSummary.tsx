import { formatRupees } from "../lib/format";
import type { GiftBuilder } from "../hooks/useGiftBuilder";

interface Props {
  builder: GiftBuilder;
  /** Delivery charge once the PIN code is known. */
  deliveryCharge: number | null;
}

export default function CheckoutSummary({ builder, deliveryCharge }: Props) {
  const {
    selectedPackaging,
    packagingQuantity,
    slots,
    packagingPrice,
    productTotal,
    totalWeight,
  } = builder;

  if (!selectedPackaging) return null;

  const isJar = selectedPackaging.kind === "jar";
  const total = productTotal + packagingPrice + (deliveryCharge ?? 0);

  return (
    <div className="sticky top-6 self-start rounded-3xl bg-white p-6 shadow-xl">
      <h2 className="mb-4 text-2xl font-bold text-gray-900">
        Order Summary
      </h2>

      <p className="font-semibold">
        {packagingQuantity} × {selectedPackaging.name}
      </p>
      <p className="mb-3 text-sm text-gray-500">
        Each {isJar ? "jar set" : "box"} contains:
      </p>

      <ul className="mb-4 space-y-1 text-sm">
        {slots.map((slot, index) =>
          slot ? (
            <li key={index} className="flex justify-between">
              <span>
                {slot.product.emoji} {slot.product.name} · {slot.weight}g
              </span>
              <span>{formatRupees(slot.price)}</span>
            </li>
          ) : null
        )}
      </ul>

      <hr className="my-4 border-gray-200" />

      <dl className="space-y-2">
        <div className="flex justify-between">
          <dt>Products</dt>
          <dd>{formatRupees(productTotal)}</dd>
        </div>
        <div className="flex justify-between">
          <dt>Packaging</dt>
          <dd>{formatRupees(packagingPrice)}</dd>
        </div>
        <div className="flex justify-between">
          <dt>Delivery</dt>
          <dd>
            {deliveryCharge === null
              ? "Enter PIN code"
              : formatRupees(deliveryCharge)}
          </dd>
        </div>
        <div className="flex justify-between text-sm text-gray-500">
          <dt>Total weight</dt>
          <dd>{totalWeight}g</dd>
        </div>
      </dl>

      <div className="mt-4 flex justify-between rounded-xl bg-green-50 p-4 text-xl">
        <span>Total</span>
        <span className="font-bold text-green-700">
          {formatRupees(total)}
        </span>
      </div>

      {deliveryCharge === null && (
        <p className="mt-2 text-sm text-gray-500">
          Final total is shown once delivery is calculated.
        </p>
      )}
    </div>
  );
}
