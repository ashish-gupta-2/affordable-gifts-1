import { useCallback, useState, type FormEvent } from "react";
import { formatRupees } from "../lib/format";
import type { AdminApi } from "./api";
import type { AdminPackaging } from "./types";
import { inputClass, primaryButton, secondaryButton } from "./ui";
import { useResource } from "./useResource";

interface FormState {
  id: string;
  name: string;
  kind: AdminPackaging["kind"];
  description: string;
  price: string;
  slotCount: string;
  slotCapacity: string;
  displayOrder: string;
  active: boolean;
}

const blankForm: FormState = {
  id: "",
  name: "",
  kind: "box",
  description: "",
  price: "",
  slotCount: "2",
  slotCapacity: "100",
  displayOrder: "0",
  active: true,
};

function toForm(p: AdminPackaging): FormState {
  return {
    id: p.id,
    name: p.name,
    kind: p.kind,
    description: p.description,
    price: String(p.price),
    slotCount: String(p.slotCount),
    slotCapacity: String(p.slotCapacity),
    displayOrder: String(p.displayOrder),
    active: p.active,
  };
}

function PackagingForm({
  api,
  editing,
  onDone,
}: {
  api: AdminApi;
  editing: AdminPackaging | null;
  onDone: () => void;
}) {
  const [form, setForm] = useState<FormState>(editing ? toForm(editing) : blankForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const set = <K extends keyof FormState>(key: K, value: FormState[K]) =>
    setForm((current) => ({ ...current, [key]: value }));

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    const common = {
      name: form.name,
      kind: form.kind,
      description: form.description,
      price: Number(form.price),
      slotCount: Number(form.slotCount),
      slotCapacity: Number(form.slotCapacity),
      displayOrder: Number(form.displayOrder) || 0,
    };
    try {
      if (editing) {
        await api.send("PUT", `/packaging/${editing.id}`, { ...common, active: form.active });
      } else {
        await api.send("POST", "/packaging", { ...common, id: form.id });
      }
      onDone();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Save failed");
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-2xl bg-white p-6 shadow">
      <h3 className="text-lg font-bold">{editing ? `Edit ${editing.name}` : "Add packaging"}</h3>
      <div className="grid gap-4 md:grid-cols-3">
        <label className="block">
          <span className="mb-1 block text-sm font-medium">ID</span>
          <input
            required
            disabled={editing !== null}
            pattern="[a-z0-9]+(-[a-z0-9]+)*"
            title="lowercase letters, digits and hyphens"
            placeholder="box-5"
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
          <span className="mb-1 block text-sm font-medium">Type</span>
          <select className={inputClass} value={form.kind} onChange={(e) => set("kind", e.target.value as FormState["kind"])}>
            <option value="box">Gift box</option>
            <option value="jar">Glass jar set</option>
          </select>
        </label>
        <label className="block md:col-span-2">
          <span className="mb-1 block text-sm font-medium">Description</span>
          <input required maxLength={200} className={inputClass} value={form.description} onChange={(e) => set("description", e.target.value)} />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">Price (₹)</span>
          <input required type="number" min="0" step="0.01" className={inputClass} value={form.price} onChange={(e) => set("price", e.target.value)} />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">Partitions / jars</span>
          <input required type="number" min="1" className={inputClass} value={form.slotCount} onChange={(e) => set("slotCount", e.target.value)} />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium">Grams per slot</span>
          <input required type="number" min="1" className={inputClass} value={form.slotCapacity} onChange={(e) => set("slotCapacity", e.target.value)} />
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
      <p className="text-xs text-gray-500">
        A jar set must be 2 jars of 200g or 3 jars of 100g (they fit one outer cover).
      </p>
      {error && <p role="alert" className="text-sm text-red-600">{error}</p>}
      <div className="flex gap-3">
        <button type="submit" disabled={saving} className={primaryButton}>
          {saving ? "Saving…" : editing ? "Save changes" : "Add packaging"}
        </button>
        <button type="button" className={secondaryButton} onClick={onDone}>
          Cancel
        </button>
      </div>
    </form>
  );
}

export default function PackagingPanel({ api }: { api: AdminApi }) {
  const load = useCallback(() => api.get<AdminPackaging[]>("/packaging"), [api]);
  const { data, error, reload } = useResource(load);
  const [editing, setEditing] = useState<AdminPackaging | null | undefined>(undefined);
  const [actionError, setActionError] = useState<string | null>(null);

  const toggleActive = async (item: AdminPackaging) => {
    setActionError(null);
    try {
      if (item.active) {
        await api.send("DELETE", `/packaging/${item.id}`);
      } else {
        await api.send("PUT", `/packaging/${item.id}`, {
          name: item.name,
          kind: item.kind,
          description: item.description,
          price: item.price,
          slotCount: item.slotCount,
          slotCapacity: item.slotCapacity,
          displayOrder: item.displayOrder,
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
        <h2 className="text-2xl font-bold">Packaging</h2>
        {editing === undefined && (
          <button type="button" className={primaryButton} onClick={() => setEditing(null)}>
            Add packaging
          </button>
        )}
      </div>

      {editing !== undefined && (
        <PackagingForm
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
      {!data && !error && <p className="text-gray-500">Loading packaging…</p>}

      {data && (
        <div className="overflow-x-auto rounded-2xl bg-white shadow">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-gray-200 bg-gray-50 text-gray-500">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Type</th>
                <th className="px-4 py-3">Layout</th>
                <th className="px-4 py-3">Price</th>
                <th className="px-4 py-3">Order</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody>
              {data.map((item) => (
                <tr key={item.id} className={`border-b border-gray-100 ${item.active ? "" : "text-gray-400"}`}>
                  <td className="px-4 py-3 font-medium">
                    {item.name}
                    <div className="text-xs text-gray-400">{item.id}</div>
                  </td>
                  <td className="px-4 py-3">{item.kind === "box" ? "Gift box" : "Glass jars"}</td>
                  <td className="px-4 py-3">
                    {item.slotCount} × {item.slotCapacity}g
                  </td>
                  <td className="px-4 py-3">{formatRupees(item.price)}</td>
                  <td className="px-4 py-3">{item.displayOrder}</td>
                  <td className="px-4 py-3">{item.active ? "Visible" : "Hidden"}</td>
                  <td className="space-x-2 px-4 py-3 text-right">
                    <button type="button" className={secondaryButton} onClick={() => setEditing(item)}>
                      Edit
                    </button>
                    <button type="button" className={secondaryButton} onClick={() => toggleActive(item)}>
                      {item.active ? "Hide" : "Show"}
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
