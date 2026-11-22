import { formatNumber } from "@/lib/format";
import { ChangeIndicator } from "./ChangeIndicator";
import styles from "./PriceCard.module.css";

export type Decoration = "bar" | "ring" | "orb" | "coins";

type Props = {
  title: string;
  badge: string;
  /** Small label above the big number, e.g. "ขายออก". */
  label: string;
  value: string | null | undefined;
  fractionDigits?: number;
  /** Left side of the footer, e.g. "รับซื้อ 56,950.00" or "เทียบเมื่อวาน". */
  footnote: string;
  change: string | null | undefined;
  changePercent: string | null | undefined;
  decoration: Decoration;
  loading?: boolean;
};

export function PriceCard({
  title,
  badge,
  label,
  value,
  fractionDigits = 2,
  footnote,
  change,
  changePercent,
  decoration,
  loading = false,
}: Props) {
  return (
    <article className={styles.card} aria-busy={loading || undefined}>
      <div className={`${styles.decoration} ${styles[decoration]}`} aria-hidden="true">
        <span />
        <span />
      </div>

      <header className={styles.top}>
        <h3 className={styles.title}>{title}</h3>
        <span className={styles.badge}>{badge}</span>
      </header>

      <div className={styles.bottom}>
        <p className={styles.label}>{label}</p>
        {loading ? (
          <span className={styles.skeleton}>
            <span className="visually-hidden">กำลังโหลด</span>
          </span>
        ) : (
          <p className={`${styles.value} num`}>{formatNumber(value, fractionDigits)}</p>
        )}
        <div className={styles.footer}>
          <span className={`${styles.footnote} num`}>{footnote}</span>
          {loading ? null : (
            <ChangeIndicator change={change} changePercent={changePercent} fractionDigits={fractionDigits} />
          )}
        </div>
      </div>
    </article>
  );
}
