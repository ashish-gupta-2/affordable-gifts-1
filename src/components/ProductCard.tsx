import type { Product } from "../types";

interface Props {
  product: Product;
  /** Pack size (g) of the selected packaging's slots, if any. */
  slotCapacity: number | null;
  onAdd: (product: Product) => void;
  blockReason: string | null;
}

export default function ProductCard({
  product,
  slotCapacity,
  onAdd,
  blockReason,
}: Props) {
  const selectedPack =
    slotCapacity === null
      ? undefined
      : product.packs.find((pack) => pack.weight === slotCapacity);

  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg">
      <div className="flex h-40 items-center justify-center bg-gradient-to-br from-yellow-50 to-orange-100">
        <span className="text-5xl">
            {product.emoji}
        </span>
      </div>

      <div className="p-5">
        <h3 className="text-lg font-semibold text-gray-900">
          {product.name}
        </h3>

        <span className="mt-3 inline-block rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700">
            {product.category}
        </span>

        <div className="mt-4">
          {selectedPack ? (
            <div className="flex items-center justify-between">
              <span className="text-xl font-bold text-green-700">
                ₹{selectedPack.price}
              </span>

              <span className="rounded-full bg-gray-100 px-3 py-1 text-sm">
                {selectedPack.weight}g
              </span>
            </div>
          ) : (
            <div className="flex flex-wrap gap-2">
              {product.packs.map((pack) => (
                <span
                  key={pack.weight}
                  className="rounded-full bg-gray-100 px-3 py-1 text-sm"
                >
                  {pack.weight}g · ₹{pack.price}
                </span>
              ))}
            </div>
          )}
        </div>

        <button
          type="button"
          onClick={() => onAdd(product)}
          disabled={blockReason !== null}
          className="mt-5 w-full rounded-xl bg-green-600 py-3 font-medium text-white transition hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:hover:bg-gray-300">
          {blockReason ?? "Add To Box"}
        </button>
      </div>
    </div>
  );
}
