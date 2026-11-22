import type { HistoryPoint, ThaiGoldLatest } from "@/lib/api";
import { formatNumber, formatThaiTime } from "@/lib/format";
import { ChangeIndicator } from "./ChangeIndicator";
import { ChartIcon, ListIcon } from "./Icons";
import { Navbar } from "./Navbar";
import styles from "./Hero.module.css";

type Props = {
  bar: ThaiGoldLatest | null | undefined;
  /** Today's hourly closes for the mini bar chart. */
  todayPoints: HistoryPoint[];
  updatedAt: string | null | undefined;
  loading: boolean;
};

const MAX_BARS = 10;

function MiniBars({ points }: { points: HistoryPoint[] }) {
  const shown = points.slice(-MAX_BARS);
  if (shown.length === 0) {
    return <p className={styles.miniEmpty}>ยังไม่มีรอบประกาศวันนี้</p>;
  }
  const values = shown.map((p) => Number(p.close));
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min;
  const label = `ราคาขายออกรายชั่วโมงวันนี้ ${shown
    .map((p) => `${formatThaiTime(p.time)} น. ${formatNumber(p.close)} บาท`)
    .join(", ")}`;

  return (
    <figure className={styles.mini} aria-label={label} role="img">
      <div className={styles.miniBars} aria-hidden="true">
        {shown.map((p, i) => {
          // keep even the lowest bar visible
          const height = span === 0 ? 70 : 25 + ((values[i] - min) / span) * 75;
          return <span key={p.time} className={styles.miniBar} style={{ height: `${height}%` }} />;
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

export function Hero({ bar, todayPoints, updatedAt, loading }: Props) {
  return (
    <section className={styles.hero} id="top" aria-labelledby="hero-title">
      {/* Decorative CSS-only art; replace with a photo through --hero-photo. */}
      <div className={styles.art} aria-hidden="true">
        <span className={styles.beam} />
        <span className={styles.beamGlow} />
        <span className={styles.water} />
      </div>

      <Navbar updatedAt={updatedAt} />

      <div className={styles.inner}>
        <div className={styles.copy}>
          <p className={styles.eyebrow}>ราคาทองคำไทย · ราคาทองโลก</p>
          <h1 id="hero-title" className={styles.title}>
            ราคาทองคำวันนี้
            <br />
            ครบ ชัด ในที่เดียว
          </h1>
          <p className={styles.lead}>
            ติดตามราคาทองคำแท่งและทองรูปพรรณ 96.5% ตามประกาศสมาคมค้าทองคำ พร้อมราคาทองโลกและค่าเงินบาท
            อัปเดตอัตโนมัติ ดูย้อนหลังได้ทั้งรายวัน รายเดือน และรายปี
          </p>
          <div className={styles.actions}>
            <a className={`${styles.button} ${styles.primary}`} href="#history">
              <ChartIcon />
              ดูกราฟย้อนหลัง
            </a>
            <a className={`${styles.button} ${styles.secondary}`} href="#announcements">
              <ListIcon />
              ประวัติการประกาศ
            </a>
          </div>
        </div>

        <aside className={styles.card} aria-label="ราคาทองคำแท่งขายออกล่าสุด">
          <p className={styles.cardLabel}>ทองคำแท่ง · ขายออก</p>
          {loading && !bar ? (
            <div className={styles.cardSkeleton} aria-busy="true">
              <span className="visually-hidden">กำลังโหลดราคา</span>
            </div>
          ) : (
            <>
              <p className={styles.price}>
                <span className="num">{formatNumber(bar?.sell)}</span>
                <span className={styles.unit}>บาท</span>
              </p>
              <div className={styles.cardMeta}>
                <ChangeIndicator change={bar?.change} changePercent={bar?.changePercent} tone="dark" chip />
                <span className={styles.compare}>เทียบเมื่อวาน</span>
              </div>
              <MiniBars points={todayPoints} />
            </>
          )}
        </aside>
      </div>
    </section>
  );
}
