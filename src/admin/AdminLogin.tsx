import { useState, type FormEvent } from "react";
import { verifyAdminKey } from "./api";
import { inputClass, primaryButton } from "./ui";

interface Props {
  onLogin: (key: string) => void;
}

export default function AdminLogin({ onLogin }: Props) {
  const [key, setKey] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [checking, setChecking] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setChecking(true);
    setError(null);
    try {
      if (await verifyAdminKey(key)) {
        onLogin(key);
      } else {
        setError("That admin key is not correct.");
      }
    } catch {
      setError("Could not reach the server. Is the backend running?");
    } finally {
      setChecking(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-6">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-sm space-y-4 rounded-3xl bg-white p-8 shadow-xl"
      >
        <h1 className="text-2xl font-bold text-green-700">
          🎁 Admin Console
        </h1>
        <p className="text-sm text-gray-500">
          Enter the admin key to manage orders and the catalogue.
        </p>

        <label className="block">
          <span className="mb-1 block text-sm font-medium">Admin key</span>
          <input
            type="password"
            required
            autoFocus
            autoComplete="current-password"
            className={inputClass}
            value={key}
            onChange={(event) => setKey(event.target.value)}
          />
        </label>

        {error && (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={checking || key === ""}
          className={`${primaryButton} w-full`}
        >
          {checking ? "Checking…" : "Sign in"}
        </button>
      </form>
    </div>
  );
}
