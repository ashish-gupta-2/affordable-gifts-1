import type { Product } from "../types";
import ProductCard from "./ProductCard";

interface Props {
  products: Product[];
  /** Pack size (g) of the selected packaging's slots, if any. */
  slotCapacity: number | null;
  onAdd: (product: Product) => void;
  getAddBlockReason: (product: Product) => string | null;
}

export default function ProductCatalog({
  products,
  slotCapacity,
  onAdd,
  getAddBlockReason,
}: Props) {
  return (
    <section>
      <div className="mb-6">
        <h2 className="text-2xl font-bold">
          Select Products
        </h2>

        <p className="text-gray-600">
          Choose dry fruits and snacks
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            slotCapacity={slotCapacity}
            onAdd={onAdd}
            blockReason={getAddBlockReason(product)}
          />
        ))}
      </div>
    </section>
  );
}
