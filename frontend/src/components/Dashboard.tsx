"use client";

import { fetchAnnouncements, fetchHistory, fetchLatest } from "@/lib/api";
import { bangkokDateKey, formatThaiDateTime } from "@/lib/format";
import { usePolling } from "@/hooks/usePolling";
import { AnnouncementTable } from "./AnnouncementTable";
import { BackToTop } from "./BackToTop";
import { Interactions } from "./Interactions";
import { Footer } from "./Footer";
import { Hero } from "./Hero";
import { AlertIcon } from "./Icons";
import { LatestPrices } from "./LatestPrices";
import { Navbar } from "./Navbar";
import { PriceHistoryChart } from "./PriceHistoryChart";
import styles from "./Dashboard.module.css";

const REFRESH_MS = 60_000;

/** The server's price is older than this when the page last fetched it -> warn that it may be behind. */
const STALE_AFTER_MS = 30 * 60_000;

/** Age (ms) of the data at the moment the browser fetched it; null when either time is unknown. */
function dataAgeMs(updatedAt: string | null | undefined, fetchedAt: string | null): number | null {
  if (!updatedAt || !fetchedAt) return null;
  const age = Date.parse(fetchedAt) - Date.parse(updatedAt);
  return Number.isNaN(age) ? null : age;
}

const loadTodayBars = (signal: AbortSignal) => fetchHistory("bar", "1D", signal);

/** The whole single-page dashboard. Data refreshes every 60 s; on API failure the last good data stays visible. */
export function Dashboard() {
  const latest = usePolling(fetchLatest, REFRESH_MS);
  const todayBars = usePolling(loadTodayBars, REFRESH_MS);
  const announcements = usePolling(fetchAnnouncements, REFRESH_MS);

  // Hero mini chart: only the hourly buckets that belong to "today" in Thailand.
  const lastPoint = todayBars.data?.points.at(-1);
  const todayKey = bangkokDateKey(lastPoint?.time);
  const todayPoints = (todayBars.data?.points ?? []).filter((p) => bangkokDateKey(p.time) === todayKey);

  // Navbar's "updated" time is when the browser fetched; this is when the price itself was last updated.
  const priceUpdatedAt = latest.data?.updatedAt ?? latest.data?.bar?.updatedAt;
  const age = dataAgeMs(priceUpdatedAt, latest.fetchedAt);
  const stale = !latest.error && age !== null && age > STALE_AFTER_MS;

  return (
    <>
      <a className={styles.skipLink} href="#today">
        ข้ามไปยังราคาล่าสุด
      </a>
      <Navbar updatedAt={latest.fetchedAt} />
      <Hero bar={latest.data?.bar} todayPoints={todayPoints} loading={latest.loading} />
      <main className={styles.main}>
        {/* ambient gold light drifting behind the content */}
        <div className={styles.ambient} aria-hidden="true">
          <span />
          <span />
        </div>
        {latest.error ? (
          <div className={styles.notice} role="status">
            <AlertIcon />
            <span>
              {latest.data
                ? "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ชั่วคราว ขณะนี้แสดงราคาล่าสุดที่มีอยู่ และระบบจะลองใหม่อัตโนมัติ"
                : "ยังโหลดราคาไม่ได้ในขณะนี้ ระบบจะลองใหม่อัตโนมัติทุก 1 นาที"}
            </span>
          </div>
        ) : null}
        {stale ? (
          <div className={styles.notice} role="status">
            <AlertIcon />
            <span>
              ข้อมูลราคาอัปเดตล่าสุดเมื่อ {formatThaiDateTime(priceUpdatedAt)} หากราคาไม่ตรงกับเว็บสมาคมค้าทองคำ
              ระบบอาจยังไม่ได้ดึงราคารอบใหม่
            </span>
          </div>
        ) : null}
        <LatestPrices data={latest.data} loading={latest.loading} />
        <PriceHistoryChart />
        <AnnouncementTable data={announcements.data} loading={announcements.loading} />
      </main>
      <Footer />
      <BackToTop />
      <Interactions />
    </>
  );
}
