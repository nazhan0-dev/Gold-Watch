"use client";

import { useEffect, useRef, type CSSProperties, type ElementType, type ReactNode } from "react";

type Props = {
  children: ReactNode;
  /** Element to render; defaults to a div. */
  as?: ElementType;
  /** Stagger delay in ms, applied when the element scrolls into view. */
  delay?: number;
  className?: string;
};

/**
 * Fades and lifts its content in the first time it scrolls into view.
 * The hidden state lives in globals.css under `@media (scripting: enabled)`,
 * so without JavaScript (or with reduced motion) the content is simply visible.
 */
export function Reveal({ children, as: Tag = "div", delay = 0, className }: Props) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const node = ref.current;
    if (!node) return;
    if (typeof IntersectionObserver === "undefined") {
      node.dataset.reveal = "shown";
      return;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            (entry.target as HTMLElement).dataset.reveal = "shown";
            observer.unobserve(entry.target);
          }
        }
      },
      { rootMargin: "0px 0px -8% 0px", threshold: 0.12 },
    );
    observer.observe(node);
    return () => observer.disconnect();
  }, []);

  return (
    <Tag
      ref={ref}
      data-reveal=""
      className={className}
      style={delay ? ({ "--reveal-delay": `${delay}ms` } as CSSProperties) : undefined}
    >
      {children}
    </Tag>
  );
}
