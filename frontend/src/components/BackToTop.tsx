"use client";

import { useEffect, useState } from "react";
import styles from "./BackToTop.module.css";

const SHOW_AFTER_PX = 700;
const R = 20;
const CIRCUMFERENCE = 2 * Math.PI * R;

/** Floating "back to top" button whose gold ring fills as the page is read. */
export function BackToTop() {
  const [progress, setProgress] = useState(0);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    let frame = 0;
    const measure = () => {
      frame = 0;
      const max = document.documentElement.scrollHeight - window.innerHeight;
      setProgress(max > 0 ? Math.min(1, window.scrollY / max) : 0);
      setVisible(window.scrollY > SHOW_AFTER_PX);
    };
    const onScroll = () => {
      if (!frame) frame = requestAnimationFrame(measure);
    };
    measure();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => {
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("resize", onScroll);
      cancelAnimationFrame(frame);
    };
  }, []);

  return (
    <a
      href="#top"
      className={`${styles.button} ${visible ? styles.visible : ""}`}
      aria-label="กลับขึ้นด้านบน"
      tabIndex={visible ? undefined : -1}
      data-magnetic
    >
      <svg viewBox="0 0 48 48" width="48" height="48" aria-hidden="true">
        <circle className={styles.track} cx="24" cy="24" r={R} />
        <circle
          className={styles.ring}
          cx="24"
          cy="24"
          r={R}
          strokeDasharray={CIRCUMFERENCE}
          strokeDashoffset={CIRCUMFERENCE * (1 - progress)}
        />
        <path className={styles.arrow} d="M24 31V17M18 23l6-6 6 6" />
      </svg>
    </a>
  );
}
