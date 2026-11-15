import { useEffect, useRef, useState, type FormEvent } from "react";
import { fetchDeliveryQuote, placeOrder } from "../api/orders";
import CheckoutSummary from "../components/CheckoutSummary";
import type { GiftBuilder } from "../hooks/useGiftBuilder";
import type { CheckoutDetails, DeliveryQuote, PlacedOrder } from "../types";

interface Props {
  builder: GiftBuilder;
  /** Held by the parent so the form survives "Back to edit your box". */
  details: CheckoutDetails;
  onDetailsChange: (details: CheckoutDetails) => void;
  onBack: () => void;
  onPlaced: (order: PlacedOrder) => void;
}

const inputClass =
  "w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-green-600 focus:outline-none focus:ring-1 focus:ring-green-600";

export default function CheckoutPage({
  builder,
  details,
  onDetailsChange,
  onBack,
  onPlaced,
}: Props) {
  const [quote, setQuote] = useState<DeliveryQuote | null>(null);
  const [quoteError, setQuoteError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  // One id per checkout attempt: if the request is retried the backend
  // returns the same order instead of creating a duplicate.
  const requestId = useRef(crypto.randomUUID());

  const { selectedPackaging, packagingQuantity, slots } = builder;

  useEffect(() => {
    if (!/^[1-9][0-9]{5}$/.test(details.pincode)) return;

    let cancelled = false;
    fetchDeliveryQuote(details.pincode)
      .then((result) => {
        if (cancelled) return;
        setQuote(result);
        setQuoteError(null);
      })
      .catch((error: Error) => {
        if (!cancelled) setQuoteError(error.message);
      });

    return () => {
      cancelled = true;
    };
  }, [details.pincode]);

  // Ignore a quote that belongs to a PIN code the customer has since edited.
  const activeQuote = quote?.pincode === details.pincode ? quote : null;

  const update =
    (field: keyof CheckoutDetails) =>
    (event: { target: { value: string } }) =>
      onDetailsChange({ ...details, [field]: event.target.value });

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!selectedPackaging || submitting) return;

    setSubmitting(true);
    setSubmitError(null);
    try {
      const order = await placeOrder({
        requestId: requestId.current,
        packagingId: selectedPackaging.id,
        quantity: packagingQuantity,
        productIds: slots.map((slot) => slot?.product.id ?? ""),
        details,
      });
      onPlaced(order);
    } catch (error) {
      setSubmitError(
        error instanceof Error ? error.message : "Could not place the order."
      );
      setSubmitting(false);
    }
  };

  return (
    <div className="grid gap-10 lg:grid-cols-[2.5fr_1fr]">
      <form onSubmit={handleSubmit} className="space-y-6">
        <button
          type="button"
          onClick={onBack}
          className="text-sm font-medium text-green-700 hover:underline"
        >
          ← Back to edit your box
        </button>

        <section className="rounded-3xl bg-white p-6 shadow">
          <h2 className="mb-4 text-xl font-bold">Contact details</h2>
          <div className="grid gap-4 md:grid-cols-2">
            <label className="block md:col-span-2">
              <span className="mb-1 block text-sm font-medium">Full name</span>
              <input
                required
                maxLength={120}
                autoComplete="name"
                className={inputClass}
                value={details.name}
                onChange={update("name")}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium">
                Mobile number
              </span>
              <input
                required
                type="tel"
                inputMode="numeric"
                autoComplete="tel-national"
                pattern="[6-9][0-9]{9}"
                title="10-digit Indian mobile number"
                maxLength={10}
                className={inputClass}
                value={details.phone}
                onChange={update("phone")}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium">
                Email (optional)
              </span>
              <input
                type="email"
                maxLength={160}
                autoComplete="email"
                className={inputClass}
                value={details.email}
                onChange={update("email")}
              />
            </label>
          </div>
        </section>

        <section className="rounded-3xl bg-white p-6 shadow">
          <h2 className="mb-4 text-xl font-bold">Delivery address</h2>
          <div className="grid gap-4 md:grid-cols-2">
            <label className="block">
              <span className="mb-1 block text-sm font-medium">
                House / apartment no.
              </span>
              <input
                required
                maxLength={120}
                className={inputClass}
                value={details.house}
                onChange={update("house")}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium">Street</span>
              <input
                required
                maxLength={160}
                className={inputClass}
                value={details.street}
                onChange={update("street")}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium">
                Area / locality
              </span>
              <input
                required
                maxLength={120}
                className={inputClass}
                value={details.area}
                onChange={update("area")}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium">PIN code</span>
              <input
                required
                inputMode="numeric"
                autoComplete="postal-code"
                pattern="[1-9][0-9]{5}"
                title="6-digit PIN code"
                maxLength={6}
                className={inputClass}
                value={details.pincode}
                onChange={update("pincode")}
              />
              {activeQuote && (
                <span className="mt-1 block text-sm text-green-700">
                  {activeQuote.zone === "local"
                    ? "Local Bengaluru delivery"
                    : "India-wide delivery"}
                </span>
              )}
              {quoteError && details.pincode.length === 6 && (
                <span className="mt-1 block text-sm text-red-600">
                  {quoteError}
                </span>
              )}
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium">City</span>
              <input
                required
                maxLength={80}
                className={inputClass}
                value={details.city}
                onChange={update("city")}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium">State</span>
              <input
                required
                maxLength={80}
                className={inputClass}
                value={details.state}
                onChange={update("state")}
              />
            </label>
            <label className="block md:col-span-2">
              <span className="mb-1 block text-sm font-medium">
                Delivery instructions (optional)
              </span>
              <textarea
                maxLength={300}
                rows={2}
                className={inputClass}
                value={details.instructions}
                onChange={update("instructions")}
              />
            </label>
          </div>
        </section>

        {submitError && (
          <p
            role="alert"
            className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-700"
          >
            {submitError}
          </p>
        )}

        <button
          type="submit"
          disabled={submitting || !activeQuote}
          className="w-full rounded-xl bg-green-600 py-3 text-lg font-semibold text-white transition hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:hover:bg-gray-300"
        >
          {submitting ? "Placing order…" : "Place Order"}
        </button>
        <p className="text-sm text-gray-500">
          Online payment is not set up yet. Your order is saved and we will
          contact you to confirm payment.
        </p>
      </form>

      <CheckoutSummary
        builder={builder}
        deliveryCharge={activeQuote?.charge ?? null}
      />
    </div>
  );
}
