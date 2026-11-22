import { GoldBarIcon } from "./Icons";
import styles from "./Footer.module.css";

export function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.inner}>
        <p className={styles.disclaimer}>
          ข้อมูลบนเว็บไซต์นี้ใช้เพื่อการติดตามราคาเท่านั้น ไม่ใช่คำแนะนำการลงทุน
        </p>
        <div className={styles.sources}>
          <h2 className={styles.heading}>แหล่งที่มาของข้อมูล</h2>
          <ul>
            <li>
              ราคาทองคำไทย:{" "}
              <a href="https://www.goldtraders.or.th/" target="_blank" rel="noopener noreferrer">
                สมาคมค้าทองคำ (goldtraders.or.th)
              </a>
            </li>
            <li>
              ราคาทองโลก XAU/USD:{" "}
              <a href="https://gold-api.com/" target="_blank" rel="noopener noreferrer">
                gold-api.com
              </a>
            </li>
            <li>
              อัตราแลกเปลี่ยน USD/THB:{" "}
              <a href="https://www.exchangerate-api.com" target="_blank" rel="noopener noreferrer">
                Rates By Exchange Rate API
              </a>{" "}
              (อัปเดตวันละครั้ง)
            </li>
          </ul>
          <p className={styles.note}>
            ราคาอาจล่าช้ากว่าแหล่งต้นทางได้สูงสุดประมาณ 5 นาที เวลาทั้งหมดแสดงเป็นเวลาประเทศไทย
          </p>
        </div>
        <p className={styles.brand}>
          <GoldBarIcon width={22} height={22} className={styles.logo} />
          GoldWatch · โปรเจกต์ฝึกงาน
        </p>
      </div>
    </footer>
  );
}
