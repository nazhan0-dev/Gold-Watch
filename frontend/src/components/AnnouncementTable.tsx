import type { Announcements } from "@/lib/api";
import { formatNumber, formatThaiDate, formatThaiTime } from "@/lib/format";
import type { CSSProperties } from "react";
import { ChangeIndicator } from "./ChangeIndicator";
import { Reveal } from "./Reveal";
import { SectionHeader } from "./SectionHeader";
import sectionStyles from "./Section.module.css";
import styles from "./AnnouncementTable.module.css";

type Props = { data: Announcements | null; loading: boolean };

/** Section 4: today's announcement rounds, newest first. */
export function AnnouncementTable({ data, loading }: Props) {
  const rounds = data?.rounds ?? [];
  // Noon in Bangkok on that date, so the formatted date can never slip a day.
  const dateLabel = data ? formatThaiDate(`${data.date}T05:00:00Z`) : null;

  return (
    <section id="announcements" className={sectionStyles.section} aria-labelledby="announcements-title">
      <div className={sectionStyles.inner}>
        <SectionHeader
          id="announcements-title"
          kicker="Announcements"
          title="ประวัติการประกาศวันนี้"
          subtitle={dateLabel}
          description={
            <>
              ราคาทองคำไทยประกาศโดยสมาคมค้าทองคำ และอาจปรับหลายรอบต่อวันตามความเคลื่อนไหวของตลาด โดยคอลัมน์ “เปลี่ยนแปลง”
              เทียบราคาขายออกทองคำแท่งกับรอบก่อนหน้า
            </>
          }
        />

        <Reveal className={styles.card}>
          <div className={styles.scroller} role="region" aria-labelledby="announcements-title" tabIndex={0}>
            <table className={styles.table}>
              <caption className="visually-hidden">รอบประกาศราคาทองคำวันนี้ เรียงจากล่าสุด ราคาเป็นบาท</caption>
              <thead>
                <tr>
                  <th scope="col">ครั้งที่</th>
                  <th scope="col">เวลา</th>
                  <th scope="col" className={styles.numeric}>
                    ทองคำแท่ง รับซื้อ
                  </th>
                  <th scope="col" className={styles.numeric}>
                    ทองคำแท่ง ขายออก
                  </th>
                  <th scope="col" className={styles.numeric}>
                    ทองรูปพรรณ ขายออก
                  </th>
                  <th scope="col" className={styles.numeric}>
                    เปลี่ยนแปลง
                  </th>
                </tr>
              </thead>
              <tbody>
                {rounds.map((round, i) => (
                  <tr key={round.announcedAt} style={{ "--row": i } as CSSProperties}>
                    <td className="num">{round.round}</td>
                    <td className="num">{formatThaiTime(round.announcedAt)} น.</td>
                    <td className={`${styles.numeric} num`}>{formatNumber(round.barBuy)}</td>
                    <td className={`${styles.numeric} num`}>{formatNumber(round.barSell)}</td>
                    <td className={`${styles.numeric} num`}>{formatNumber(round.ornamentSell)}</td>
                    <td className={styles.numeric}>
                      <ChangeIndicator change={round.change} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {rounds.length === 0 ? (
              <p className={styles.empty}>{loading ? "กำลังโหลด…" : "ยังไม่มีการประกาศราคาในวันนี้"}</p>
            ) : null}
          </div>
        </Reveal>
      </div>
    </section>
  );
}
