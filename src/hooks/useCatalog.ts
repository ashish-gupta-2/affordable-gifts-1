import { useEffect, useState } from "react";
import {
  fetchMinOrderValue,
  fetchPackagingOptions,
  fetchProducts,
} from "../api/catalog";
import type { Packaging, Product } from "../types";

export function useCatalog() {
  const [products, setProducts] = useState<Product[]>([]);
  const [packagingOptions, setPackagingOptions] = useState<Packaging[]>([]);
  const [minOrderValue, setMinOrderValue] = useState(0);
  const [status, setStatus] = useState<"loading" | "ready" | "error">(
    "loading"
  );

  useEffect(() => {
    let cancelled = false;

    Promise.all([
      fetchProducts(),
      fetchPackagingOptions(),
      fetchMinOrderValue(),
    ])
      .then(([loadedProducts, loadedPackaging, loadedMinimum]) => {
        if (cancelled) return;
        setProducts(loadedProducts);
        setPackagingOptions(loadedPackaging);
        setMinOrderValue(loadedMinimum);
        setStatus("ready");
      })
      .catch(() => {
        if (!cancelled) setStatus("error");
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return { products, packagingOptions, minOrderValue, status };
}
