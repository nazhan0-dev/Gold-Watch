"use client";

import { useEffect, useRef } from "react";
import styles from "./AnimatedValue.module.css";

type Props = { text: string; className?: string };

const COUNT_MS = 1400;

/** "57,150.00" -> { value: 57150, decimals: 2 }; null for anything that is not a plain number. */
function parseNumber(text: string): { value: number; decimals: number } | null {
  if (!/^-?[\d,]+(\.\d+)?$/.test(text)) return null;
  const value = Number(text.replace(/,/g, ""));
  if (!Number.isFinite(value)) return null;
  return { value, decimals: text.split(".")[1]?.length ?? 0 };
}

/**
 * Re-mounts whenever the displayed text changes, so a fresh price rolls up into place.
 * The first time a number scrolls into view it also counts up from zero. The markup always
 * holds the exact final text (server render, tests, no-JS and reduced motion keep it as is);
 * only the visible digits are animated, and they always finish on that exact text.
 */
export function AnimatedValue({ text, className }: Props) {
  return <Counter key={text} text={text} className={className} />;
}

function Counter({ text, className }: Props) {
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    const el = ref.current;
    const parsed = parseNumber(text);
    if (!el || !parsed || parsed.value === 0) return;
    if (typeof IntersectionObserver === "undefined") return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    // count up once per spot on the page; later price updates just roll in
    const host = el.parentElement;
    if (!host || host.dataset.counted) return;

    const format = new Intl.NumberFormat("en-US", {
      minimumFractionDigits: parsed.decimals,
      maximumFractionDigits: parsed.decimals,
    });
    let frame = 0;

    const run = () => {
      const start = performance.now();
      const tick = (now: number) => {
        const t = Math.min(1, (now - start) / COUNT_MS);
        const eased = 1 - Math.pow(2, -10 * t); // ease-out expo: fast, then settles
        el.textContent = t < 1 ? format.format(parsed.value * eased) : text;
        if (t < 1) frame = requestAnimationFrame(tick);
      };
      frame = requestAnimationFrame(tick);
    };

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((e) => e.isIntersecting)) {
          observer.disconnect();
          host.dataset.counted = "1"; // marked only once counting really starts
          run();
        }
      },
      { threshold: 0.6 },
    );
    observer.observe(el);

    return () => {
      observer.disconnect();
      cancelAnimationFrame(frame);
      el.textContent = text;
    };
  }, [text]);

  return (
    <span ref={ref} className={`${styles.value} ${className ?? ""}`}>
      {text}
    </span>
  );
}
