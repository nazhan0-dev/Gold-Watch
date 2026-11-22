export type Theme = "light" | "dark";

/** localStorage key holding the visitor's explicit choice (absent = follow the OS setting). */
export const THEME_STORAGE_KEY = "goldwatch-theme";

/**
 * Runs inline in <head> before first paint so the page never flashes the wrong theme.
 * Kept as a plain string: it must not depend on the bundle.
 */
export const THEME_INIT_SCRIPT = `(function(){var t;try{t=localStorage.getItem("${THEME_STORAGE_KEY}")}catch(e){}if(t!=="light"&&t!=="dark"){t=window.matchMedia&&window.matchMedia("(prefers-color-scheme: light)").matches?"light":"dark"}document.documentElement.dataset.theme=t})();`;
