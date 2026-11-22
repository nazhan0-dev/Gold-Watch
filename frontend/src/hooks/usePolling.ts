"use client";

import { useEffect, useRef, useState } from "react";

export type PollingState<T> = {
  /** Last successful response; kept when later requests fail. */
  data: T | null;
  /** True while the first request (or a request after `key` changed) is in flight with no data to show. */
  loading: boolean;
  /** Message of the most recent failure, cleared on the next success. */
  error: string | null;
};

/**
 * Calls `fetcher` now, whenever `key` changes, and then every `intervalMs`.
 * Responses from superseded requests are ignored, so quick filter changes never show stale data.
 */
export function usePolling<T>(
  fetcher: (signal: AbortSignal) => Promise<T>,
  intervalMs: number,
  key = "",
): PollingState<T> {
  const [state, setState] = useState<PollingState<T> & { key: string }>({
    data: null,
    loading: true,
    error: null,
    key,
  });
  const fetcherRef = useRef(fetcher);

  useEffect(() => {
    fetcherRef.current = fetcher;
  });

  useEffect(() => {
    let active = true;
    let controller: AbortController | null = null;

    const run = async () => {
      controller?.abort();
      controller = new AbortController();
      try {
        const data = await fetcherRef.current(controller.signal);
        if (active) setState({ data, loading: false, error: null, key });
      } catch (error) {
        if (!active || (error instanceof DOMException && error.name === "AbortError")) return;
        const message = error instanceof Error ? error.message : "เกิดข้อผิดพลาด";
        setState((prev) => ({
          // keep showing the last good data for the same key
          data: prev.key === key ? prev.data : null,
          loading: false,
          error: message,
          key,
        }));
      }
    };

    run();
    const timer = window.setInterval(run, intervalMs);
    return () => {
      active = false;
      window.clearInterval(timer);
      controller?.abort();
    };
  }, [intervalMs, key]);

  // While a new key loads, don't show data that belongs to the previous key.
  if (state.key !== key) {
    return { data: null, loading: true, error: null };
  }
  return { data: state.data, loading: state.loading, error: state.error };
}
