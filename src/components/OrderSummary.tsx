import type { Packaging } from "../types";
import type { Slot } from "../hooks/useGiftBuilder";

interface Props {
  selectedPackaging: Packaging | null;
  packagingQuantity: number;
  packagingPrice: number;
  onIncreasePackaging: () => void;
  onDecreasePackaging: () => void;
  slots: Slot[];
  totalPrice: number;
  totalWeight: number;
  remainingAmount: number;
  minOrderValue: number;
  emptySlotCount: number;
  onClearSlot: (index: number) => void;
  canCheckout: boolean;
  onCheckout: () => void;
}

export default function OrderSummary({
  selectedPackaging,
  packagingQuantity,
  packagingPrice,
  onIncreasePackaging,
  onDecreasePackaging,
  slots,
  totalPrice,
  totalWeight,
  remainingAmount,
  minOrderValue,
  emptySlotCount,
  onClearSlot,
  canCheckout,
  onCheckout,
}: Props) {
  const isJar = selectedPackaging?.kind === "jar";
  const slotLabel = isJar ? "Jar" : "Partition";
  const setLabel = isJar ? "jar sets" : "boxes";

  return (
    <div className="sticky top-6 self-start rounded-3xl bg-white p-6 shadow-xl">
      <h2 className="mb-6 text-2xl font-bold text-gray-900">
        🛒 Your Box
      </h2>

      {!selectedPackaging ? (
        <div className="rounded-lg bg-gray-50 p-4 text-gray-500">
          <div className="py-10 text-center">
            <div className="text-5xl">🎁</div>
            <p className="mt-4 text-gray-500">
              Choose packaging to start
            </p>
          </div>
        </div>
      ) : (
        <div className="space-y-3">
          {packagingQuantity > 1 && (
            <p className="rounded-lg bg-blue-50 px-3 py-2 text-sm text-blue-700">
              All {packagingQuantity} {setLabel} will be packed with the
              same items.
            </p>
          )}

          {slots.map((slot, index) => (
            <div
              key={index}
              className="rounded-xl border border-gray-100 bg-gray-50 p-4"
            >
              <div className="text-xs font-semibold uppercase tracking-wide text-gray-400">
                {slotLabel} {index + 1} · {selectedPackaging.slotCapacity}g
              </div>

              {slot ? (
                <div className="mt-1 flex items-center justify-between gap-3">
                  <div>
                    <p className="font-semibold">
                      {slot.product.emoji} {slot.product.name}
                    </p>
                    <p className="text-sm text-gray-500">
                      {slot.weight}g
                    </p>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className="font-bold text-green-700">
                      ₹{slot.price}
                    </span>
                    <button
                      type="button"
                      aria-label={`Remove ${slot.product.name}`}
                      onClick={() => onClearSlot(index)}
                      className="flex h-8 w-8 items-center justify-center rounded-full bg-red-500 font-bold text-white transition hover:bg-red-600"
                    >
                      ×
                    </button>
                  </div>
                </div>
              ) : (
                <p className="mt-1 text-gray-400">Empty</p>
              )}
            </div>
          ))}
        </div>
      )}

      <hr className="my-6 border-gray-200" />

      {selectedPackaging && (
        <div className="mb-4 rounded-lg bg-green-50 p-3">
          <div className="font-medium">Selected Packaging</div>
          <div className="flex justify-between">
            <span>{selectedPackaging.name}</span>
            <span>₹{packagingPrice}</span>
          </div>
          <p className="text-sm text-gray-500">
            ₹{selectedPackaging.price} each
          </p>

          <div className="mt-3 flex items-center gap-3">
            <button
              type="button"
              aria-label="Remove one"
              onClick={onDecreasePackaging}
              disabled={packagingQuantity <= 1}
              className="flex h-9 w-9 items-center justify-center rounded-full bg-red-500 font-bold text-white transition hover:bg-red-600 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:hover:bg-gray-300"
            >
              -
            </button>

            <span
              aria-label="Packaging quantity"
              className="min-w-[30px] text-center text-lg font-semibold"
            >
              {packagingQuantity}
            </span>

            <button
              type="button"
              aria-label="Add one more"
              onClick={onIncreasePackaging}
              className="flex h-9 w-9 items-center justify-center rounded-full bg-green-600 font-bold text-white transition hover:bg-green-700"
            >
              +
            </button>
          </div>
        </div>
      )}

      <div className="mt-6 rounded-xl bg-green-50 p-4">
        <div className="mb-2 flex justify-between">
          <span>Total Weight</span>
          <span className="font-semibold">{totalWeight}g</span>
        </div>

        <div className="flex justify-between text-xl">
          <span>Total Price</span>

          <span className="font-bold text-green-700">
            ₹{totalPrice}
          </span>
        </div>
      </div>

      {selectedPackaging && emptySlotCount > 0 && (
        <div className="mt-6 rounded-xl border border-amber-200 bg-amber-50 p-4 text-amber-700">
          Fill {emptySlotCount} more{" "}
          {slotLabel.toLowerCase()}
          {emptySlotCount > 1 ? "s" : ""} to complete your box
        </div>
      )}

      {remainingAmount > 0 ? (
        <div className="mt-6 rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">
          Add ₹{remainingAmount} more to reach
          the minimum order value of ₹{minOrderValue}
        </div>
      ) : (
        <div className="mt-6 rounded-xl border border-green-200 bg-green-50 p-4 text-green-700">
          ✅ Minimum order reached
        </div>
      )}

      <button
        type="button"
        onClick={onCheckout}
        disabled={!canCheckout}
        className="mt-6 w-full rounded-xl bg-green-600 py-3 text-lg font-semibold text-white transition hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:hover:bg-gray-300"
      >
        Proceed to Checkout
      </button>
    </div>
  );
}
