"use client";

import { useEffect, useRef, useState, type CSSProperties } from "react";
import { formatThaiDate, formatThaiShortDate, formatThaiTime } from "@/lib/format";
import { GoldWatchMark } from "./GoldWatchMark";
import { ThemeToggle } from "./ThemeToggle";
import styles from "./Navbar.module.css";

const NAV_LINKS = [
  { href: "#today", label: "ราคาวันนี้" },
  { href: "#history", label: "กราฟย้อนหลัง" },
  { href: "#announcements", label: "ประวัติการประกาศ" },
] as const;

type Props = {
  /** When the page last received fresh prices (page load/refresh and every poll), ISO. */
  updatedAt: string | null | undefined;
};

/** Section currently under the navbar, or null while the hero is showing. */
function useActiveSection(): string | null {
  const [active, setActive] = useState<string | null>(null);

  useEffect(() => {
    const ids = NAV_LINKS.map((link) => link.href.slice(1));
    const update = () => {
      // the last section whose top has passed a line 35% down the viewport
      const line = window.innerHeight * 0.35;
      let current: string | null = null;
      for (const id of ids) {
        const el = document.getElementById(id);
        if (el && el.getBoundingClientRect().top <= line) current = `#${id}`;
      }
      setActive(current);
    };
    update();
    window.addEventListener("scroll", update, { passive: true });
    window.addEventListener("resize", update);
    return () => {
      window.removeEventListener("scroll", update);
      window.removeEventListener("resize", update);
    };
  }, []);

  return active;
}

/** Fixed top bar: open over the hero, a floating glass bar once the page scrolls. */
export function Navbar({ updatedAt }: Props) {
  const [scrolled, setScrolled] = useState(false);
  const active = useActiveSection();
  const listRef = useRef<HTMLUListElement>(null);
  const [indicator, setIndicator] = useState<{ x: number; w: number } | null>(null);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  // Move the gold pill under the active link (re-measured on resize and font load).
  useEffect(() => {
    const measure = () => {
      const link = active ? listRef.current?.querySelector<HTMLAnchorElement>(`a[href="${active}"]`) : null;
      setIndicator(link ? { x: link.offsetLeft, w: link.offsetWidth } : null);
    };
    measure();
    window.addEventListener("resize", measure);
    document.fonts?.ready.then(measure);
    return () => window.removeEventListener("resize", measure);
  }, [active]);

  return (
    <header className={`${styles.navbar} ${scrolled ? styles.scrolled : ""}`}>
      <div className={styles.bar}>
        <a href="#top" className={styles.brand} aria-label="GoldWatch หน้าแรก">
          <GoldWatchMark className={styles.logo} width={40} height={40} />
          <span className={styles.brandText}>
            <span className={styles.wordmark}>GoldWatch</span>
            <span className={styles.tagline} aria-hidden="true">
              Thai Gold Price
            </span>
          </span>
        </a>

        <nav aria-label="เมนูหลัก" className={styles.nav}>
          <ul
            ref={listRef}
            style={
              indicator
                ? ({ "--ind-x": `${indicator.x}px`, "--ind-w": `${indicator.w}px` } as CSSProperties)
                : undefined
            }
            data-indicator={indicator ? "on" : "off"}
          >
            {NAV_LINKS.map((link) => (
              <li key={link.href}>
                <a href={link.href} aria-current={active === link.href ? "location" : undefined}>
                  {link.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>

        <div className={styles.actions}>
          <ThemeToggle />
          <p className={styles.status} aria-live="polite">
            <span className={styles.live} aria-hidden="true">
              <span className={styles.dot} />
              Live
            </span>
            <span key={updatedAt ?? "loading"} className={`${styles.time} num`}>
              {updatedAt ? (
                <>
                  {/* label hides visually on phones but is still read out */}
                  <span className={styles.timeLabel}>อัปเดตล่าสุด </span>
                  <span className={`${styles.date} ${styles.dateLong}`}>{formatThaiDate(updatedAt)}</span>
                  <span className={`${styles.date} ${styles.dateShort}`}>{formatThaiShortDate(updatedAt)}</span>
                  <span className={styles.sep} aria-hidden="true" />
                  {formatThaiTime(updatedAt)} น.
                </>
              ) : (
                "กำลังโหลด…"
              )}
            </span>
          </p>
        </div>
      </div>
    </header>
  );
}
