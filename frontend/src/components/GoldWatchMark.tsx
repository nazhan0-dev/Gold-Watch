import { useId, type SVGProps } from "react";
import styles from "./GoldWatchMark.module.css";

type Props = SVGProps<SVGSVGElement> & { title?: string };

/**
 * GoldWatch logo mark (H2): an hourglass drawn in fine lines with gold sand —
 * watching the price of gold over time.
 * Animated: the top bulb drains, a stream of sand falls, the pile grows, then the glass
 * turns over and starts again; a small glint flashes now and then (all CSS, still under reduced motion).
 * The glass outline and falling sand follow `color` (set it to the theme's gold);
 * caps and sand use a fixed gold gradient. Decorative by default; pass a title to expose it.
 */
export function GoldWatchMark({ title, className, ...props }: Props) {
  // unique gradient id so several marks can sit on one page
  const id = useId().replace(/:/g, "");
  const gold = `gw-gold-${id}`;

  return (
    <svg
      width={32}
      height={32}
      viewBox="0 0 64 64"
      aria-hidden={title ? undefined : true}
      role={title ? "img" : undefined}
      focusable="false"
      className={`${styles.mark} ${className ?? ""}`}
      {...props}
    >
      {title ? <title>{title}</title> : null}
      <defs>
        <linearGradient id={gold} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#F9DF94" />
          <stop offset="55%" stopColor="#D9A646" />
          <stop offset="100%" stopColor="#A2711F" />
        </linearGradient>
      </defs>
      <g className={styles.glass}>
        {/* top and bottom caps */}
        <rect x={11} y={7} width={42} height={5} rx={2.5} fill={`url(#${gold})`} />
        <rect x={11} y={52} width={42} height={5} rx={2.5} fill={`url(#${gold})`} />
        {/* glass outline */}
        <path
          d="M17 12 C17 23 28 27 30.5 32 C28 37 17 41 17 52 M47 12 C47 23 36 27 33.5 32 C36 37 47 41 47 52"
          fill="none"
          stroke="currentColor"
          strokeWidth={2.6}
          strokeLinecap="round"
        />
        {/* sand: upper bulb (drains), falling stream, pile below (grows) */}
        <path
          className={styles.sandTop}
          d="M22.5 20 H41.5 C39 25 35 27.5 32 30.5 C29 27.5 25 25 22.5 20 Z"
          fill={`url(#${gold})`}
        />
        <path
          className={styles.stream}
          d="M32 31 V46"
          stroke="currentColor"
          strokeWidth={1.6}
          strokeLinecap="round"
          strokeDasharray="2 2.5"
        />
        <path
          className={styles.sandBottom}
          d="M20.5 49.5 C23 43.5 28 41.5 32 41 C36 41.5 41 43.5 43.5 49.5 Z"
          fill={`url(#${gold})`}
        />
      </g>
      {/* four-point glint by the top cap */}
      <path
        className={styles.glint}
        d="M52 4 L53.2 7.8 L57 9 L53.2 10.2 L52 14 L50.8 10.2 L47 9 L50.8 7.8 Z"
        fill="#FFF3CC"
      />
    </svg>
  );
}
