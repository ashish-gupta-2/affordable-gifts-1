import { useCallback, useEffect, useState } from "react";

/** Loads data with `load` and lets the panel refetch after a change. */
export function useResource<T>(load: () => Promise<T>) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let cancelled = false;
    load()
      .then((result) => {
        if (cancelled) return;
        setData(result);
        setError(null);
      })
      .catch((e: Error) => {
        if (!cancelled) setError(e.message);
      });
    return () => {
      cancelled = true;
    };
  }, [load, version]);

  const reload = useCallback(() => setVersion((v) => v + 1), []);
  return { data, error, reload };
}
