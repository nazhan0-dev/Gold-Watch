"use client";

import { useEffect } from "react";

const MAX_TILT_DEG = 5;
const MAGNET_PULL = 0.22;

/**
 * Page-wide pointer effects, wired with one delegated listener (no per-card handlers):
 *  - [data-tilt]: the element tips toward the cursor and gets --mx/--my (cursor position in %)
 *    for a spotlight; styles live with each component.
 *  - [data-magnetic]: the element drifts a little toward the cursor (--tx/--ty in px).
 * Mouse/trackpad only, and off under reduced motion. Renders nothing.
 */
export function Interactions() {
  useEffect(() => {
    const fine = window.matchMedia("(hover: hover) and (pointer: fine)");
    const reduce = window.matchMedia("(prefers-reduced-motion: reduce)");
    let tilted: HTMLElement | null = null;
    let magnet: HTMLElement | null = null;
    let frame = 0;
    let last: PointerEvent | null = null;

    const reset = (el: HTMLElement | null, vars: string[]) => {
      if (!el) return;
      for (const v of vars) el.style.removeProperty(v);
    };

    const update = () => {
      frame = 0;
      const e = last;
      if (!e) return;
      const target = e.target instanceof Element ? e.target : null;

      const tilt = target?.closest<HTMLElement>("[data-tilt]") ?? null;
      if (tilt !== tilted) {
        reset(tilted, ["--rx", "--ry", "--mx", "--my"]);
        tilted = tilt;
      }
      if (tilt) {
        const r = tilt.getBoundingClientRect();
        const px = (e.clientX - r.left) / r.width; // 0..1
        const py = (e.clientY - r.top) / r.height;
        tilt.style.setProperty("--ry", `${((px - 0.5) * 2 * MAX_TILT_DEG).toFixed(2)}deg`);
        tilt.style.setProperty("--rx", `${((0.5 - py) * 2 * MAX_TILT_DEG).toFixed(2)}deg`);
        tilt.style.setProperty("--mx", `${(px * 100).toFixed(1)}%`);
        tilt.style.setProperty("--my", `${(py * 100).toFixed(1)}%`);
      }

      const mag = target?.closest<HTMLElement>("[data-magnetic]") ?? null;
      if (mag !== magnet) {
        reset(magnet, ["--tx", "--ty"]);
        magnet = mag;
      }
      if (mag) {
        const r = mag.getBoundingClientRect();
        mag.style.setProperty("--tx", `${((e.clientX - (r.left + r.width / 2)) * MAGNET_PULL).toFixed(1)}px`);
        mag.style.setProperty("--ty", `${((e.clientY - (r.top + r.height / 2)) * MAGNET_PULL).toFixed(1)}px`);
      }
    };

    const onMove = (e: PointerEvent) => {
      if (!fine.matches || reduce.matches || e.pointerType !== "mouse") return;
      last = e;
      if (!frame) frame = requestAnimationFrame(update); // at most once per frame
    };
    const onLeave = () => {
      reset(tilted, ["--rx", "--ry", "--mx", "--my"]);
      reset(magnet, ["--tx", "--ty"]);
      tilted = magnet = null;
    };

    document.addEventListener("pointermove", onMove, { passive: true });
    document.documentElement.addEventListener("pointerleave", onLeave);
    return () => {
      document.removeEventListener("pointermove", onMove);
      document.documentElement.removeEventListener("pointerleave", onLeave);
      cancelAnimationFrame(frame);
    };
  }, []);

  return null;
}
