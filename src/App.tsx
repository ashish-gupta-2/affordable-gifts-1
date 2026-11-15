import { useState } from "react";
import SiteHeader from "./components/SiteHeader";
import { useCatalog } from "./hooks/useCatalog";
import { useGiftBuilder } from "./hooks/useGiftBuilder";
import BuildBoxPage from "./pages/BuildBoxPage";
import CheckoutPage from "./pages/CheckoutPage";
import OrderConfirmationPage from "./pages/OrderConfirmationPage";
import { emptyDetails } from "./lib/checkoutDefaults";
import type { CheckoutDetails, PlacedOrder } from "./types";

type Step = "build" | "checkout" | "confirmed";

function App() {
  const { products, packagingOptions, minOrderValue, status } = useCatalog();
  // Held here (not in the page) so "Back to edit" keeps the customer's box.
  const builder = useGiftBuilder(minOrderValue);
  const [step, setStep] = useState<Step>("build");
  const [details, setDetails] = useState<CheckoutDetails>(emptyDetails);
  const [placedOrder, setPlacedOrder] = useState<PlacedOrder | null>(null);

  const handlePlaced = (order: PlacedOrder) => {
    setPlacedOrder(order);
    builder.reset();
    setDetails(emptyDetails);
    setStep("confirmed");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-green-50">
      <SiteHeader
        minOrderValue={status === "ready" ? minOrderValue : undefined}
      />

      <main className="mx-auto max-w-7xl p-6">
        {status === "loading" && (
          <p className="py-20 text-center text-gray-500">
            Loading catalogue…
          </p>
        )}

        {status === "error" && (
          <p className="py-20 text-center text-red-600">
            Could not load the catalogue. Please try again.
          </p>
        )}

        {status === "ready" && step === "build" && (
          <BuildBoxPage
            products={products}
            packagingOptions={packagingOptions}
            minOrderValue={minOrderValue}
            builder={builder}
            onCheckout={() => setStep("checkout")}
          />
        )}

        {status === "ready" && step === "checkout" && (
          <CheckoutPage
            builder={builder}
            details={details}
            onDetailsChange={setDetails}
            onBack={() => setStep("build")}
            onPlaced={handlePlaced}
          />
        )}

        {status === "ready" && step === "confirmed" && placedOrder && (
          <OrderConfirmationPage
            order={placedOrder}
            onNewOrder={() => setStep("build")}
          />
        )}
      </main>
    </div>
  );
}

export default App;
