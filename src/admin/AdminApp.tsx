import { useCallback, useMemo, useState } from "react";
import AdminLogin from "./AdminLogin";
import OrdersPanel from "./OrdersPanel";
import PackagingPanel from "./PackagingPanel";
import ProductsPanel from "./ProductsPanel";
import SettingsPanel from "./SettingsPanel";
import { createAdminApi } from "./api";

const KEY_STORAGE = "affordable-gifts-admin-key";

type Tab = "orders" | "products" | "packaging" | "settings";

const TABS: { id: Tab; label: string }[] = [
  { id: "orders", label: "Orders" },
  { id: "products", label: "Products" },
  { id: "packaging", label: "Packaging" },
  { id: "settings", label: "Settings" },
];

export default function AdminApp() {
  // sessionStorage: the key survives a refresh but not closing the tab.
  const [key, setKey] = useState(() => sessionStorage.getItem(KEY_STORAGE));
  const [tab, setTab] = useState<Tab>("orders");

  const logout = useCallback(() => {
    sessionStorage.removeItem(KEY_STORAGE);
    setKey(null);
  }, []);

  const api = useMemo(
    () => (key ? createAdminApi(key, logout) : null),
    [key, logout]
  );

  if (!key || !api) {
    return (
      <AdminLogin
        onLogin={(newKey) => {
          sessionStorage.setItem(KEY_STORAGE, newKey);
          setKey(newKey);
        }}
      />
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white shadow-sm">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <h1 className="text-2xl font-bold text-green-700">🎁 Admin Console</h1>
          <div className="flex items-center gap-4 text-sm">
            <a href="/" className="text-gray-600 hover:underline">
              View store
            </a>
            <button type="button" onClick={logout} className="font-medium text-red-600 hover:underline">
              Sign out
            </button>
          </div>
        </div>
        <nav className="mx-auto flex max-w-6xl gap-1 px-6">
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              onClick={() => setTab(t.id)}
              className={`border-b-2 px-4 py-2 text-sm font-medium ${
                tab === t.id
                  ? "border-green-600 text-green-700"
                  : "border-transparent text-gray-500 hover:text-gray-800"
              }`}
            >
              {t.label}
            </button>
          ))}
        </nav>
      </header>

      <main className="mx-auto max-w-6xl p-6">
        {tab === "orders" && <OrdersPanel api={api} />}
        {tab === "products" && <ProductsPanel api={api} />}
        {tab === "packaging" && <PackagingPanel api={api} />}
        {tab === "settings" && <SettingsPanel api={api} />}
      </main>
    </div>
  );
}
