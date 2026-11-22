import type { LatestPrices as LatestPricesData } from "@/lib/api";
import { formatNumber } from "@/lib/format";
import { PriceCard } from "./PriceCard";
import styles from "./Section.module.css";

type Props = { data: LatestPricesData | null; loading: boolean };

/** Section 2: four latest-price cards. */
export function LatestPrices({ data, loading }: Props) {
  const waiting = loading && !data;
  return (
    <section id="today" className={styles.section} aria-labelledby="today-title">
      <div className={styles.inner}>
        <header className={styles.header}>
          <h2 id="today-title" className={styles.title}>
            ราคาล่าสุดทุกประเภท
            <span className={styles.subtitle}>อัปเดตตามรอบประกาศ</span>
          </h2>
          <p className={styles.description}>
            ราคาทองคำไทยเป็นราคาต่อน้ำหนัก 1 บาท ความบริสุทธิ์ 96.5% ตามประกาศสมาคมค้าทองคำ
            ส่วนราคาทองโลกเป็นดอลลาร์สหรัฐต่อทรอยออนซ์
          </p>
        </header>

        <div className={styles.cardGrid}>
          <PriceCard
            title="ทองคำแท่ง"
            badge="96.5%"
            label="ขายออก (บาท)"
            value={data?.bar?.sell}
            footnote={`รับซื้อ ${formatNumber(data?.bar?.buy)}`}
            change={data?.bar?.change}
            changePercent={data?.bar?.changePercent}
            decoration="bar"
            loading={waiting}
          />
          <PriceCard
            title="ทองรูปพรรณ"
            badge="96.5%"
            label="ขายออก (บาท)"
            value={data?.ornament?.sell}
            footnote={`รับซื้อ ${formatNumber(data?.ornament?.buy)}`}
            change={data?.ornament?.change}
            changePercent={data?.ornament?.changePercent}
            decoration="ring"
            loading={waiting}
          />
          <PriceCard
            title="ทองโลก"
            badge="XAU/USD"
            label="ดอลลาร์ต่อออนซ์"
            value={data?.spot?.price}
            footnote="เทียบเมื่อวาน"
            change={data?.spot?.change}
            changePercent={data?.spot?.changePercent}
            decoration="orb"
            loading={waiting}
          />
          <PriceCard
            title="ค่าเงินบาท"
            badge="USD/THB"
            label="บาทต่อดอลลาร์"
            value={data?.usdThb?.price}
            fractionDigits={4}
            footnote="เทียบเมื่อวาน"
            change={data?.usdThb?.change}
            changePercent={data?.usdThb?.changePercent}
            decoration="coins"
            loading={waiting}
          />
        </div>
      </div>
    </section>
  );
}
