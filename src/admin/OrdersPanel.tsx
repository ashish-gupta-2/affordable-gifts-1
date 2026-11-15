import { useCallback, useState } from "react";
import { formatRupees } from "../lib/format";
import type { AdminApi } from "./api";
import { ORDER_STATUSES, type OrderDetail, type OrderStatus, type OrderSummary } from "./types";
import { inputClass, primaryButton, secondaryButton } from "./ui";
import { useResource } from "./useResource";

const statusLabel = (status: OrderStatus) =>
  ORDER_STATUSES.find((s) => s.value === status)?.label ?? status;

const formatDate = (iso: string) =>
  new Date(iso).toLocaleString("en-IN", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });

function OrderDetailView({
  api,
  orderNumber,
  onClose,
  onChanged,
}: {
  api: AdminApi;
  orderNumber: string;
  onClose: () => void;
  onChanged: () => void;
}) {
  const load = useCallback(
    () => api.get<OrderDetail>(`/orders/${orderNumber}`),
    [api, orderNumber]
  );
  const { data, error, reload } = useResource(load);
  const [saving, setSaving] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const update = async (body: { status?: OrderStatus; paymentStatus?: string }) => {
    setSaving(true);
    setActionError(null);
    try {
      await api.send("PATCH", `/orders/${orderNumber}`, body);
      reload();
      onChanged();
    } catch (e) {
      setActionError(e instanceof Error ? e.message : "Update failed");
    } finally {
      setSaving(false);
    }
  };

  if (error) return <p className="text-red-600">{error}</p>;
  if (!data) return <p className="text-gray-500">Loading order…</p>;

  const { order } = data;
  const closed = order.status === "DELIVERED" || order.status === "CANCELLED";

  return (
    <div className="rounded-2xl bg-white p-6 shadow">
      <div className="mb-4 flex items-start justify-between">
        <div>
          <h3 className="text-xl font-bold">{order.orderNumber}</h3>
          <p className="text-sm text-gray-500">{formatDate(order.createdAt)}</p>
        </div>
        <button type="button" className={secondaryButton} onClick={onClose}>
          Close
        </button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <div className="space-y-4">
          <div>
            <h4 className="font-semibold">Customer</h4>
            <p>{order.customerName}</p>
            <p className="text-sm text-gray-600">{order.customerPhone}</p>
            {data.customerEmail && (
              <p className="text-sm text-gray-600">{data.customerEmail}</p>
            )}
          </div>
          <div>
            <h4 className="font-semibold">Deliver to</h4>
            <p className="text-sm text-gray-600">{order.addressLine}</p>
            {data.deliveryInstructions && (
              <p className="mt-1 text-sm italic text-gray-600">
                “{data.deliveryInstructions}”
              </p>
            )}
          </div>
          <div>
            <h4 className="font-semibold">Update status</h4>
            <div className="mt-1 flex gap-2">
              <select
                aria-label="Order status"
                className={inputClass}
                value={order.status}
                disabled={saving || closed}
                onChange={(event) =>
                  update({ status: event.target.value as OrderStatus })
                }
              >
                {ORDER_STATUSES.map((s) => (
                  <option key={s.value} value={s.value}>
                    {s.label}
                  </option>
                ))}
              </select>
            </div>
            {closed && (
              <p className="mt-1 text-sm text-gray-500">
                This order is {statusLabel(order.status).toLowerCase()} and can
                no longer change status.
              </p>
            )}
          </div>
          <div>
            <h4 className="font-semibold">Payment</h4>
            <div className="mt-1 flex items-center gap-3">
              <span
                className={`rounded-full px-3 py-1 text-sm font-medium ${
                  order.paymentStatus === "PAID"
                    ? "bg-green-100 text-green-700"
                    : "bg-amber-100 text-amber-700"
                }`}
              >
                {order.paymentStatus === "PAID" ? "Paid" : "Pending"}
              </span>
              <button
                type="button"
                disabled={saving}
                className={order.paymentStatus === "PAID" ? secondaryButton : primaryButton}
                onClick={() =>
                  update({
                    paymentStatus: order.paymentStatus === "PAID" ? "PENDING" : "PAID",
                  })
                }
              >
                {order.paymentStatus === "PAID" ? "Mark unpaid" : "Mark paid"}
              </button>
            </div>
          </div>
          {actionError && (
            <p role="alert" className="text-sm text-red-600">
              {actionError}
            </p>
          )}
        </div>

        <div>
          <h4 className="font-semibold">
            {order.quantity} × {order.packagingName}
          </h4>
          <ul className="mt-1 space-y-1 text-sm">
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
            Items are for one set; every set is identical.
          </p>
          <dl className="mt-4 space-y-1 text-sm">
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
            <div className="flex justify-between text-gray-500">
              <dt>Packed weight</dt>
              <dd>{order.totalWeightGrams}g</dd>
            </div>
            <div className="flex justify-between border-t border-gray-200 pt-2 text-base font-bold">
              <dt>Total</dt>
              <dd className="text-green-700">{formatRupees(order.total)}</dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  );
}

export default function OrdersPanel({ api }: { api: AdminApi }) {
  const [filter, setFilter] = useState<OrderStatus | "">("");
  const [selected, setSelected] = useState<string | null>(null);

  const load = useCallback(
    () =>
      api.get<OrderSummary[]>(filter ? `/orders?status=${filter}` : "/orders"),
    [api, filter]
  );
  const { data, error, reload } = useResource(load);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Orders</h2>
        <div className="flex items-center gap-3">
          <select
            aria-label="Filter by status"
            className={`${inputClass} w-auto`}
            value={filter}
            onChange={(event) => setFilter(event.target.value as OrderStatus | "")}
          >
            <option value="">All statuses</option>
            {ORDER_STATUSES.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </select>
          <button type="button" className={secondaryButton} onClick={reload}>
            Refresh
          </button>
        </div>
      </div>

      {selected && (
        <OrderDetailView
          key={selected}
          api={api}
          orderNumber={selected}
          onClose={() => setSelected(null)}
          onChanged={reload}
        />
      )}

      {error && <p className="text-red-600">{error}</p>}
      {!data && !error && <p className="text-gray-500">Loading orders…</p>}

      {data && data.length === 0 && (
        <p className="rounded-2xl bg-white p-8 text-center text-gray-500 shadow">
          No orders{filter ? " with this status" : " yet"}.
        </p>
      )}

      {data && data.length > 0 && (
        <div className="overflow-x-auto rounded-2xl bg-white shadow">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-gray-200 bg-gray-50 text-gray-500">
              <tr>
                <th className="px-4 py-3">Order</th>
                <th className="px-4 py-3">Placed</th>
                <th className="px-4 py-3">Customer</th>
                <th className="px-4 py-3">Items</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Payment</th>
                <th className="px-4 py-3 text-right">Total</th>
              </tr>
            </thead>
            <tbody>
              {data.map((order) => (
                <tr
                  key={order.orderNumber}
                  onClick={() => setSelected(order.orderNumber)}
                  className={`cursor-pointer border-b border-gray-100 hover:bg-green-50 ${
                    selected === order.orderNumber ? "bg-green-50" : ""
                  }`}
                >
                  <td className="px-4 py-3 font-semibold">{order.orderNumber}</td>
                  <td className="px-4 py-3 text-gray-600">{formatDate(order.createdAt)}</td>
                  <td className="px-4 py-3">
                    {order.customerName}
                    <div className="text-xs text-gray-500">{order.customerPhone}</div>
                  </td>
                  <td className="px-4 py-3">
                    {order.quantity} × {order.packagingName}
                  </td>
                  <td className="px-4 py-3">{statusLabel(order.status)}</td>
                  <td className="px-4 py-3">
                    {order.paymentStatus === "PAID" ? "Paid" : "Pending"}
                  </td>
                  <td className="px-4 py-3 text-right font-semibold">
                    {formatRupees(order.total)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
