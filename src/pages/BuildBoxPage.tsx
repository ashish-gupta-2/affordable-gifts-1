import ProductCatalog from "../components/ProductCatalog";
import PackagingSelector from "../components/PackagingSelector";
import OrderSummary from "../components/OrderSummary";
import type { GiftBuilder } from "../hooks/useGiftBuilder";
import type { Packaging, Product } from "../types";

interface Props {
  products: Product[];
  packagingOptions: Packaging[];
  minOrderValue: number;
  builder: GiftBuilder;
  onCheckout: () => void;
}

export default function BuildBoxPage({
  products,
  packagingOptions,
  minOrderValue,
  builder,
  onCheckout,
}: Props) {
  const {
    selectedPackaging,
    selectPackaging,
    packagingQuantity,
    increasePackaging,
    decreasePackaging,
    packagingPrice,
    slots,
    addProduct,
    clearSlot,
    getAddBlockReason,
    totalPrice,
    totalWeight,
    remainingAmount,
    emptySlotCount,
    canCheckout,
  } = builder;

  return (
    <div className="grid gap-10 lg:grid-cols-[2.5fr_1fr]">
      <div>
        <PackagingSelector
          packagingOptions={packagingOptions}
          selectedPackaging={selectedPackaging}
          onSelect={selectPackaging}
        />
        <ProductCatalog
          products={products}
          slotCapacity={selectedPackaging?.slotCapacity ?? null}
          onAdd={addProduct}
          getAddBlockReason={getAddBlockReason}
        />
      </div>

      <OrderSummary
        selectedPackaging={selectedPackaging}
        packagingQuantity={packagingQuantity}
        packagingPrice={packagingPrice}
        onIncreasePackaging={increasePackaging}
        onDecreasePackaging={decreasePackaging}
        slots={slots}
        totalPrice={totalPrice}
        totalWeight={totalWeight}
        remainingAmount={remainingAmount}
        minOrderValue={minOrderValue}
        emptySlotCount={emptySlotCount}
        onClearSlot={clearSlot}
        canCheckout={canCheckout}
        onCheckout={onCheckout}
      />
    </div>
  );
}
