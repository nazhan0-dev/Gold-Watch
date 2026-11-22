import type { LatestPrices as LatestPricesData } from "@/lib/api";
import { formatNumber } from "@/lib/format";
import { PriceCard } from "./PriceCard";
import { Reveal } from "./Reveal";
import { SectionHeader } from "./SectionHeader";
import styles from "./Section.module.css";

type Props = { data: LatestPricesData | null; loading: boolean };

const STAGGER_MS = 110;

/**
 * Footer note beside the change figure. "เทียบกับเมื่อวาน" only makes sense when there is a
 * change to show; otherwise the card would read "compared with yesterday · no data to compare".
 */
const compareNote = (change: string | null | undefined, fallback: string) =>
  change != null ? "เทียบกับเมื่อวาน" : fallback;

/** Section 2: four latest-price cards. */
export function LatestPrices({ data, loading }: Props) {
  const waiting = loading && !data;
  return (
    <section id="today" className={styles.section} aria-labelledby="today-title">
      <div className={styles.inner}>
        <SectionHeader
          id="today-title"
          kicker="Today"
          title="ราคาล่าสุดทุกประเภท"
          subtitle="อัปเดตอัตโนมัติ ไม่ต้องรีเฟรชหน้า"
          description={
            <>
              ราคาทองคำไทยเป็นราคาต่อน้ำหนัก 1 บาท ความบริสุทธิ์ 96.5% ตามประกาศสมาคมค้าทองคำ ราคาทองโลกเป็นดอลลาร์สหรัฐต่อทรอยออนซ์
              และค่าเงินบาทเป็นบาทต่อ 1 ดอลลาร์สหรัฐ
            </>
          }
        />

        <div className={styles.cardGrid}>
          <Reveal>
            <PriceCard
              title="ทองคำแท่ง"
              badge="96.5%"
              label="ขายออก (บาท)"
              value={data?.bar?.sell}
              footnote={`รับซื้อ ${formatNumber(data?.bar?.buy)}`}
              change={data?.bar?.change}
              changePercent={data?.bar?.changePercent}
              kind="bar"
              loading={waiting}
            />
          </Reveal>
          <Reveal delay={STAGGER_MS}>
            <PriceCard
              title="ทองรูปพรรณ"
              badge="96.5%"
              label="ขายออก (บาท)"
              value={data?.ornament?.sell}
              footnote={`รับซื้อ ${formatNumber(data?.ornament?.buy)}`}
              change={data?.ornament?.change}
              changePercent={data?.ornament?.changePercent}
              kind="ornament"
              loading={waiting}
            />
          </Reveal>
          <Reveal delay={STAGGER_MS * 2}>
            <PriceCard
              title="ทองโลก"
              badge="XAU/USD"
              label="ดอลลาร์ต่อทรอยออนซ์"
              value={data?.spot?.price}
              footnote={compareNote(data?.spot?.change, "ราคาตลาดโลก")}
              change={data?.spot?.change}
              changePercent={data?.spot?.changePercent}
              kind="spot"
              loading={waiting}
            />
          </Reveal>
          <Reveal delay={STAGGER_MS * 3}>
            <PriceCard
              title="ค่าเงินบาท"
              badge="USD/THB"
              label="บาทต่อดอลลาร์"
              value={data?.usdThb?.price}
              fractionDigits={4}
              footnote={compareNote(data?.usdThb?.change, "อัตราอ้างอิง")}
              change={data?.usdThb?.change}
              changePercent={data?.usdThb?.changePercent}
              kind="usdThb"
              loading={waiting}
            />
          </Reveal>
        </div>
      </div>
    </section>
  );
}
