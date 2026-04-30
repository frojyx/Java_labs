import { useEffect, useState, type DependencyList } from "react";
import { getApiErrorMessage } from "@/lib/api";

export function useResource<T>(loader: () => Promise<T>, deps: DependencyList = []) {
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function reload() {
    setIsLoading(true);
    setError(null);
    try {
      const nextData = await loader();
      setData(nextData);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void reload();
  }, deps);

  return {
    data,
    isLoading,
    error,
    reload,
    setData,
  };
}
