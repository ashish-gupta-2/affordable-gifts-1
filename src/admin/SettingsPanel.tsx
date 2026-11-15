import { useCallback, useState, type FormEvent } from "react";
import type { AdminApi } from "./api";
import type { AdminSettings } from "./types";
import { inputClass, primaryButton } from "./ui";
import { useResource } from "./useResource";

const FIELDS: { key: keyof AdminSettings; label: string; hint: string }[] = [
  {
    key: "minOrderValue",
    label: "Minimum order value (₹)",
    hint: "Products only, before packaging and delivery.",
  },
  {
    key: "localDeliveryCharge",
    label: "Local Bengaluru delivery (₹)",
    hint: "Applied to PIN codes starting 560.",
  },
  {
    key: "indiaDeliveryCharge",
    label: "India-wide delivery (₹)",
    hint: "Applied to every other PIN code.",
  },
];

function SettingsForm({
  api,
  initial,
}: {
  api: AdminApi;
  initial: AdminSettings;
}) {
  const [values, setValues] = useState({
    minOrderValue: String(initial.minOrderValue),
    localDeliveryCharge: String(initial.localDeliveryCharge),
    indiaDeliveryCharge: String(initial.indiaDeliveryCharge),
  });
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<{ ok: boolean; text: string } | null>(null);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      await api.send("PUT", "/settings", {
        minOrderValue: Number(values.minOrderValue),
        localDeliveryCharge: Number(values.localDeliveryCharge),
        indiaDeliveryCharge: Number(values.indiaDeliveryCharge),
      });
      setMessage({ ok: true, text: "Settings saved. The storefront uses them immediately." });
    } catch (e) {
      setMessage({ ok: false, text: e instanceof Error ? e.message : "Save failed" });
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-lg space-y-5 rounded-2xl bg-white p-6 shadow">
      {FIELDS.map((field) => (
        <label key={field.key} className="block">
          <span className="mb-1 block text-sm font-medium">{field.label}</span>
          <input
            required
            type="number"
            min="0"
            step="0.01"
            className={inputClass}
            value={values[field.key]}
            onChange={(event) =>
              setValues((current) => ({ ...current, [field.key]: event.target.value }))
            }
          />
          <span className="mt-1 block text-xs text-gray-500">{field.hint}</span>
        </label>
      ))}

      {message && (
        <p role={message.ok ? "status" : "alert"} className={message.ok ? "text-green-700" : "text-red-600"}>
          {message.text}
        </p>
      )}

      <button type="submit" disabled={saving} className={primaryButton}>
        {saving ? "Saving…" : "Save settings"}
      </button>
    </form>
  );
}

export default function SettingsPanel({ api }: { api: AdminApi }) {
  const load = useCallback(() => api.get<AdminSettings>("/settings"), [api]);
  const { data, error } = useResource(load);

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold">Store settings</h2>
      {error && <p className="text-red-600">{error}</p>}
      {!data && !error && <p className="text-gray-500">Loading…</p>}
      {data && <SettingsForm api={api} initial={data} />}
    </div>
  );
}
