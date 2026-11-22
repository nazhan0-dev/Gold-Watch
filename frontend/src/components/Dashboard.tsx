"use client";

import { fetchAnnouncements, fetchHistory, fetchLatest } from "@/lib/api";
import { bangkokDateKey } from "@/lib/format";
import { usePolling } from "@/hooks/usePolling";
import { AnnouncementTable } from "./AnnouncementTable";
import { Footer } from "./Footer";
import { Hero } from "./Hero";
import { AlertIcon } from "./Icons";
import { LatestPrices } from "./LatestPrices";
import { PriceHistoryChart } from "./PriceHistoryChart";
import styles from "./Dashboard.module.css";

const REFRESH_MS = 60_000;

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

  return (
    <>
      <a className={styles.skipLink} href="#today">
        ข้ามไปยังราคาล่าสุด
      </a>
      <Hero
        bar={latest.data?.bar}
        todayPoints={todayPoints}
        updatedAt={latest.data?.updatedAt}
        loading={latest.loading}
      />
      <main>
        {latest.error ? (
          <div className={styles.notice} role="status">
            <AlertIcon />
            <span>
              {latest.data
                ? "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ชั่วคราว กำลังแสดงราคาล่าสุดที่มี และจะลองใหม่อัตโนมัติ"
                : "ยังโหลดราคาไม่ได้ในขณะนี้ ระบบจะลองใหม่อัตโนมัติทุก 1 นาที"}
            </span>
          </div>
        ) : null}
        <LatestPrices data={latest.data} loading={latest.loading} />
        <PriceHistoryChart />
        <AnnouncementTable data={announcements.data} loading={announcements.loading} />
      </main>
      <Footer />
    </>
  );
}
