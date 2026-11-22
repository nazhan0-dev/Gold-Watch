import Image, { type StaticImageData } from "next/image";
import barPhoto from "@/assets/prices/bar.jpg";
import ornamentPhoto from "@/assets/prices/ornament.jpg";
import spotPhoto from "@/assets/prices/spot.jpg";
import usdThbPhoto from "@/assets/prices/usdthb.jpg";
import barPhotoLight from "@/assets/prices/bar-light.jpg";
import ornamentPhotoLight from "@/assets/prices/ornament-light.jpg";
import spotPhotoLight from "@/assets/prices/spot-light.jpg";
import usdThbPhotoLight from "@/assets/prices/usdthb-light.jpg";
import { formatNumber } from "@/lib/format";
import { AnimatedValue } from "./AnimatedValue";
import { ChangeIndicator } from "./ChangeIndicator";
import styles from "./PriceCard.module.css";

/** Which price the card shows; picks its index number and background photo. */
export type PriceKind = "bar" | "ornament" | "spot" | "usdThb";

type CardArt = {
  /** Engraved index in the card corner, e.g. "N° 01". */
  index: string;
  /** Gold on black for the dark theme, gold on ivory for the light one. */
  dark: StaticImageData;
  light: StaticImageData;
  /** Where the subject sits inside the frame (CSS object-position). */
  focus: string;
};

/** Photo sources and licences: src/assets/prices/CREDITS.md */
const ART: Record<PriceKind, CardArt> = {
  bar: { index: "01", dark: barPhoto, light: barPhotoLight, focus: "60% 30%" },
  ornament: { index: "02", dark: ornamentPhoto, light: ornamentPhotoLight, focus: "50% 50%" },
  spot: { index: "03", dark: spotPhoto, light: spotPhotoLight, focus: "50% 20%" },
  usdThb: { index: "04", dark: usdThbPhoto, light: usdThbPhotoLight, focus: "50% 35%" },
};

const PHOTO_SIZES = "(max-width: 720px) 100vw, (max-width: 1080px) 50vw, 320px";

type Props = {
  title: string;
  badge: string;
  /** Small label above the big number, e.g. "ขายออก". */
  label: string;
  value: string | null | undefined;
  fractionDigits?: number;
  /** Left side of the footer, e.g. "รับซื้อ 56,950.00" or "เทียบกับเมื่อวาน". */
  footnote: string;
  change: string | null | undefined;
  changePercent: string | null | undefined;
  kind: PriceKind;
  loading?: boolean;
};

/** Luxury price card: gold photo fading into the card, engraved index, fine gold rule, big serif price. */
export function PriceCard({
  title,
  badge,
  label,
  value,
  fractionDigits = 2,
  footnote,
  change,
  changePercent,
  kind,
  loading = false,
}: Props) {
  const art = ART[kind];
  return (
    <article className={styles.card} aria-busy={loading || undefined} data-tilt>
      {/* decorative photo fading towards the price; CSS shows the one for the active theme
          (lazy-loaded, so the hidden one is never downloaded) */}
      <div className={styles.media} aria-hidden="true">
        {(["dark", "light"] as const).map((theme) => (
          <Image
            key={theme}
            src={art[theme]}
            alt=""
            fill
            placeholder="blur"
            sizes={PHOTO_SIZES}
            className={`${styles.photo} ${theme === "dark" ? styles.photoDark : styles.photoLight}`}
            style={{ objectPosition: art.focus }}
          />
        ))}
      </div>
      <span className={styles.shine} aria-hidden="true" />
      <span className={styles.spot} aria-hidden="true" />

      <header className={styles.top}>
        <div>
          <span className={styles.index} aria-hidden="true">
            N° {art.index}
          </span>
          <h3 className={styles.title}>{title}</h3>
        </div>
        <span className={styles.badge}>{badge}</span>
      </header>

      <span className={styles.rule} aria-hidden="true" />

      <div className={styles.bottom}>
        <p className={styles.label}>{label}</p>
        {loading ? (
          <span className={styles.skeleton}>
            <span className="visually-hidden">กำลังโหลด</span>
          </span>
        ) : (
          <p className={`${styles.value} num`}>
            <AnimatedValue text={formatNumber(value, fractionDigits)} className={styles.valueText} />
          </p>
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
