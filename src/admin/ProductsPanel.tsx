import { useCallback, useState, type FormEvent } from "react";
import { formatRupees } from "../lib/format";
import type { AdminApi } from "./api";
import type { AdminProduct } from "./types";
import { inputClass, primaryButton, secondaryButton } from "./ui";
import { useResource } from "./useResource";

interface FormState {
  id: string;
  name: string;
  category: AdminProduct["category"];
  emoji: string;
  displayOrder: string;
  active: boolean;
  price100: string;
  price200: string;
}

const blankForm: FormState = {
  id: "",
  name: "",
  category: "Dry Fruits",
  emoji: "",
  displayOrder: "0",
  active: true,
  price100: "",
  price200: "",
};

function toForm(product: AdminProduct): FormState {
  const price = (weight: number) =>
    String(product.packs.find((p) => p.weight === weight)?.price ?? "");
  return {
    id: product.id,
    name: product.name,
    category: product.category,
    emoji: product.emoji,
    displayOrder: String(product.displayOrder),
    active: product.active,
    price100: price(100),
    price200: price(200),
  };
}

/** A pack is sold only when its price is filled in. */
function buildPacks(form: FormState) {
  const packs: { weight: number; price: number }[] = [];
  if (form.price100 !== "") packs.push({ weight: 100, price: Number(form.price100) });
  if (form.price200 !== "") packs.push({ weight: 200, price: Number(form.price200) });
  return packs;
}

function ProductForm({
  api,
  editing,
  onDone,
}: {
  api: AdminApi;
  editing: AdminProduct | null;
  onDone: () => void;
}) {
  const [form, setForm] = useState<FormState>(editing ? toForm(editing) : blankForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const set = <K extends keyof FormState>(key: K, value: FormState[K]) =>
    setForm((current) => ({ ...current, [key]: value }));

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    const packs = buildPacks(form);
    if (packs.length === 0) {
      setError("Enter a price for at least one pack size.");
      return;
    }
    setSaving(true);
    setError(null);
    const common = {
      name: form.name,
      category: form.category,
      emoji: form.emoji,
      displayOrder: Number(form.displayOrder) || 0,
      packs,
    };
    try {
      if (editing) {
        await api.send("PUT", `/products/${editing.id}`, { ...common, active: form.active });
      } else {
        await api.send("POST", "/products", { ...common, id: form.id });
      }
      onDone();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Save failed");
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-2xl bg-white p-6 shadow">
      <h3 className="text-lg font-bold">{editing ? `Edit ${editing.name}` : "Add product"}</h3>
      <div className="grid gap-4 md:grid-cols-3">
        <label className="block">
          <span className="mb-1 block text-sm font-medium">ID</span>
          <input
            required
            disabled={editing !== null}
            pattern="[a-z0-9]+(-[a-z0-9]+)*"
            title="lowercase letters, digits and hyphens"
            placeholder="pistachios"
            className={inputClass}
            value={form.id}
            onChange={(e) => set("id", e.target.value)}
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">Name</span>
          <input required maxLength={120} className={inputClass} value={form.name} onChange={(e) => set("name", e.target.value)} />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">Emoji</span>
          <input required maxLength={16} className={inputClass} value={form.emoji} onChange={(e) => set("emoji", e.target.value)} />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">Category</span>
          <select className={inputClass} value={form.category} onChange={(e) => set("category", e.target.value as FormState["category"])}>
            <option>Dry Fruits</option>
            <option>Snacks</option>
          </select>
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">100g pack price (₹)</span>
          <input type="number" min="0" step="0.01" className={inputClass} value={form.price100} onChange={(e) => set("price100", e.target.value)} />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">200g pack price (₹)</span>
          <input type="number" min="0" step="0.01" className={inputClass} value={form.price200} onChange={(e) => set("price200", e.target.value)} />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">Display order</span>
          <input type="number" className={inputClass} value={form.displayOrder} onChange={(e) => set("displayOrder", e.target.value)} />
        </label>
        {editing && (
          <label className="flex items-end gap-2 pb-2">
            <input type="checkbox" checked={form.active} onChange={(e) => set("active", e.target.checked)} />
            <span className="text-sm font-medium">Visible in the store</span>
          </label>
        )}
      </div>
      <p className="text-xs text-gray-500">Leave a pack price empty if that size is not sold.</p>
      {error && <p role="alert" className="text-sm text-red-600">{error}</p>}
      <div className="flex gap-3">
        <button type="submit" disabled={saving} className={primaryButton}>
          {saving ? "Saving…" : editing ? "Save changes" : "Add product"}
        </button>
        <button type="button" className={secondaryButton} onClick={onDone}>
          Cancel
        </button>
      </div>
    </form>
  );
}

export default function ProductsPanel({ api }: { api: AdminApi }) {
  const load = useCallback(() => api.get<AdminProduct[]>("/products"), [api]);
  const { data, error, reload } = useResource(load);
  // undefined = form closed, null = adding, product = editing
  const [editing, setEditing] = useState<AdminProduct | null | undefined>(undefined);
  const [actionError, setActionError] = useState<string | null>(null);

  const toggleActive = async (product: AdminProduct) => {
    setActionError(null);
    try {
      if (product.active) {
        await api.send("DELETE", `/products/${product.id}`);
      } else {
        await api.send("PUT", `/products/${product.id}`, {
          name: product.name,
          category: product.category,
          emoji: product.emoji,
          displayOrder: product.displayOrder,
          packs: product.packs,
          active: true,
        });
      }
      reload();
    } catch (e) {
      setActionError(e instanceof Error ? e.message : "Update failed");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Products</h2>
        {editing === undefined && (
          <button type="button" className={primaryButton} onClick={() => setEditing(null)}>
            Add product
          </button>
        )}
      </div>

      {editing !== undefined && (
        <ProductForm
          key={editing?.id ?? "new"}
          api={api}
          editing={editing}
          onDone={() => {
            setEditing(undefined);
            reload();
          }}
        />
      )}

      {error && <p className="text-red-600">{error}</p>}
      {actionError && <p role="alert" className="text-red-600">{actionError}</p>}
      {!data && !error && <p className="text-gray-500">Loading products…</p>}

      {data && (
        <div className="overflow-x-auto rounded-2xl bg-white shadow">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-gray-200 bg-gray-50 text-gray-500">
              <tr>
                <th className="px-4 py-3">Product</th>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3">Pack prices</th>
                <th className="px-4 py-3">Order</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {data.map((product) => (
                <tr key={product.id} className={`border-b border-gray-100 ${product.active ? "" : "text-gray-400"}`}>
                  <td className="px-4 py-3 font-medium">
                    {product.emoji} {product.name}
                    <div className="text-xs text-gray-400">{product.id}</div>
                  </td>
                  <td className="px-4 py-3">{product.category}</td>
                  <td className="px-4 py-3">
                    {product.packs.map((p) => `${p.weight}g ${formatRupees(p.price)}`).join(" · ")}
                  </td>
                  <td className="px-4 py-3">{product.displayOrder}</td>
                  <td className="px-4 py-3">{product.active ? "Visible" : "Hidden"}</td>
                  <td className="space-x-2 px-4 py-3 text-right">
                    <button type="button" className={secondaryButton} onClick={() => setEditing(product)}>
                      Edit
                    </button>
                    <button type="button" className={secondaryButton} onClick={() => toggleActive(product)}>
                      {product.active ? "Hide" : "Show"}
                    </button>
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
