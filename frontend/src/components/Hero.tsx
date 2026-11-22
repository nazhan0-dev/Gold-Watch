import type { CSSProperties } from "react";
import type { HistoryPoint, ThaiGoldLatest } from "@/lib/api";
import { calcChange } from "@/lib/change";
import { formatNumber, formatThaiTime } from "@/lib/format";
import { AnimatedValue } from "./AnimatedValue";
import { ChangeIndicator } from "./ChangeIndicator";
import { ChartIcon, ListIcon } from "./Icons";
import styles from "./Hero.module.css";

type Props = {
  bar: ThaiGoldLatest | null | undefined;
  /** Today's hourly closes for the mini bar chart. */
  todayPoints: HistoryPoint[];
  loading: boolean;
};

const MAX_BARS = 10;

/** Floating gold dust. Positions are deterministic so server and client render the same markup. */
const PARTICLES = Array.from({ length: 26 }, (_, i) => {
  // integer hash -> [0, 1): identical on every JS engine, so no hydration mismatch
  const r = (n: number) => (((i + 1) * 2654435761 + n * 97531) % 9973) / 9973;
  return {
    left: `${(r(1) * 100).toFixed(2)}%`,
    size: `${(1.5 + r(2) * 3).toFixed(1)}px`,
    duration: `${(9 + r(3) * 12).toFixed(1)}s`,
    delay: `${(-r(4) * 20).toFixed(1)}s`,
    drift: `${((r(5) - 0.5) * 120).toFixed(0)}px`,
    opacity: (0.35 + r(6) * 0.6).toFixed(2),
  };
});

function MiniBars({ points }: { points: HistoryPoint[] }) {
  const shown = points.slice(-MAX_BARS);
  // a single bar says nothing; the stats row above already covers that case
  if (shown.length < 2) return null;
  const values = shown.map((p) => Number(p.close));
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min;
  const label = `ราคาขายออกรายชั่วโมงวันนี้ ${shown
    .map((p) => `${formatThaiTime(p.time)} น. ${formatNumber(p.close)} บาท`)
    .join(", ")}`;

  return (
    <figure className={styles.mini} aria-label={label} role="img">
      <figcaption className={styles.miniCaption} aria-hidden="true">
        รายชั่วโมงวันนี้
      </figcaption>
      <div className={styles.miniBars} aria-hidden="true">
        {shown.map((p, i) => {
          // keep even the lowest bar visible
          const height = span === 0 ? 70 : 25 + ((values[i] - min) / span) * 75;
          return (
            <span
              key={p.time}
              className={`${styles.miniBar} ${i === shown.length - 1 ? styles.miniBarLast : ""}`}
              style={{
                height: `${height}%`,
                animationDelay: `${0.9 + i * 0.07}s`,
              }}
            />
          );
        })}
      </div>
      <div className={`${styles.miniLabels} num`} aria-hidden="true">
        {shown.map((p) => (
          <span key={p.time}>{formatThaiTime(p.time)}</span>
        ))}
      </div>
    </figure>
  );
}

export function Hero({ bar, todayPoints, loading }: Props) {
  return (
    <section className={styles.hero} id="top" aria-labelledby="hero-title">
      {/* decorative CSS-only art: drifting glows, a light beam, water ripples and gold dust */}
      <div className={styles.art} aria-hidden="true">
        <span className={styles.aurora} />
        <span className={styles.auroraTwo} />
        <span className={styles.beam} />
        <span className={styles.beamGlow} />
        <span className={styles.water} />
        <div className={styles.particles}>
          {PARTICLES.map((p, i) => (
            <span
              key={i}
              style={
                {
                  left: p.left,
                  width: p.size,
                  height: p.size,
                  animationDuration: p.duration,
                  animationDelay: p.delay,
                  "--drift": p.drift,
                  "--peak": p.opacity,
                } as CSSProperties
              }
            />
          ))}
        </div>
      </div>

      <div className={styles.inner}>
        <div className={styles.copy}>
          <p className={styles.eyebrow}>
            <span className={styles.eyebrowLine} aria-hidden="true" />
            ราคาทองคำไทย · ราคาทองโลก
          </p>
          <h1 id="hero-title" className={styles.title}>
            <span className={styles.titleLine}>ราคาทองคำวันนี้</span>
            <span className={`${styles.titleLine} ${styles.titleGold} gold-text`}>ครบ ชัด ในที่เดียว</span>
          </h1>
          <p className={styles.lead}>
            ติดตามราคาทองคำแท่งและทองรูปพรรณ 96.5% ตามประกาศสมาคมค้าทองคำ พร้อมราคาทองโลกและค่าเงินบาท อัปเดตอัตโนมัติ
            และดูกราฟย้อนหลังได้ตั้งแต่ 1 วันจนถึง 1 ปี
          </p>
          <div className={styles.actions}>
            <a className={`${styles.button} ${styles.primary}`} href="#history" data-magnetic>
              <ChartIcon />
              ดูกราฟย้อนหลัง
            </a>
            <a className={`${styles.button} ${styles.secondary}`} href="#announcements" data-magnetic>
              <ListIcon />
              ประวัติการประกาศ
            </a>
          </div>
        </div>

        <aside className={styles.card} aria-label="ราคาทองคำแท่งขายออกล่าสุด" data-tilt>
          <span className={styles.cardBorder} aria-hidden="true" />
          <span className={styles.cardSheen} aria-hidden="true" />
          <div className={styles.cardHead}>
            <p className={styles.cardLabel}>ทองคำแท่ง · ขายออก</p>
            <span className={styles.cardSeal} aria-hidden="true">
              96.5
            </span>
          </div>
          {loading && !bar ? (
            <div className={styles.cardSkeleton} aria-busy="true">
              <span className="visually-hidden">กำลังโหลดราคา</span>
            </div>
          ) : (
            <>
              <p className={styles.price}>
                <AnimatedValue text={formatNumber(bar?.sell)} className={`${styles.priceValue} num gold-text`} />
                <span className={styles.unit}>บาท</span>
              </p>
              <div className={styles.cardMeta}>
                <ChangeIndicator change={bar?.change} changePercent={bar?.changePercent} chip />
                {bar?.change != null ? <span className={styles.compare}>เทียบกับเมื่อวาน</span> : null}
              </div>
              <dl className={styles.stats}>
                <div>
                  <dt>รับซื้อ</dt>
                  <dd className="num">{formatNumber(bar?.buy)}</dd>
                </div>
                <div>
                  <dt>ส่วนต่าง</dt>
                  <dd className="num">{formatNumber(calcChange(bar?.sell, bar?.buy))}</dd>
                </div>
                <div>
                  <dt>ประกาศล่าสุด</dt>
                  <dd className="num">{bar ? `${formatThaiTime(bar.updatedAt)} น.` : "–"}</dd>
                </div>
              </dl>
              <MiniBars points={todayPoints} />
            </>
          )}
        </aside>
      </div>

      <a className={styles.scrollCue} href="#today" aria-label="เลื่อนลงไปยังราคาล่าสุด">
        <span aria-hidden="true" />
      </a>
    </section>
  );
}
