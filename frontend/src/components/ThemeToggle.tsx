"use client";

import { useEffect } from "react";
import { THEME_STORAGE_KEY, type Theme } from "@/lib/theme";
import styles from "./ThemeToggle.module.css";

function currentTheme(): Theme {
  return document.documentElement.dataset.theme === "light" ? "light" : "dark";
}

function applyTheme(theme: Theme) {
  const root = document.documentElement;
  // cross-fade colours for this one switch only (see .theme-switching in globals.css)
  root.classList.add("theme-switching");
  root.dataset.theme = theme;
  window.setTimeout(() => root.classList.remove("theme-switching"), 600);
}

/**
 * Sun/moon switch. Both icons are always rendered and CSS shows the right one from
 * <html data-theme>, so server and client markup match whatever theme is active.
 */
export function ThemeToggle() {
  // Follow OS changes until the visitor picks a theme themselves.
  useEffect(() => {
    const media = window.matchMedia("(prefers-color-scheme: light)");
    const onChange = () => {
      let saved: string | null = null;
      try {
        saved = localStorage.getItem(THEME_STORAGE_KEY);
      } catch {
        // storage blocked: just follow the OS
      }
      if (saved !== "light" && saved !== "dark") applyTheme(media.matches ? "light" : "dark");
    };
    media.addEventListener("change", onChange);
    return () => media.removeEventListener("change", onChange);
  }, []);

  const toggle = () => {
    const next: Theme = currentTheme() === "light" ? "dark" : "light";
    applyTheme(next);
    try {
      localStorage.setItem(THEME_STORAGE_KEY, next);
    } catch {
      // storage blocked: the switch still works for this visit
    }
  };

  return (
    <button type="button" className={styles.toggle} onClick={toggle} aria-label="สลับธีมสว่าง / มืด">
      {/* sun: shown in dark theme (switch to light) */}
      <svg className={styles.sun} viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
        <circle cx="12" cy="12" r="4.2" />
        <g className={styles.rays}>
          <path d="M12 2.5v2.2M12 19.3v2.2M2.5 12h2.2M19.3 12h2.2M5.3 5.3l1.6 1.6M17.1 17.1l1.6 1.6M5.3 18.7l1.6-1.6M17.1 6.9l1.6-1.6" />
        </g>
      </svg>
      {/* moon: shown in light theme (switch to dark) */}
      <svg className={styles.moon} viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
        <path d="M20 14.6A8.2 8.2 0 0 1 9.4 4a8.2 8.2 0 1 0 10.6 10.6Z" />
      </svg>
    </button>
  );
}
