import { GoldWatchMark } from "./GoldWatchMark";
import { Reveal } from "./Reveal";
import styles from "./Footer.module.css";

export function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.inner}>
        <div className={styles.columns}>
          <div className={styles.intro}>
            <Reveal>
              <p className={styles.crest} aria-hidden="true">
                <GoldWatchMark className={styles.crestMark} width={56} height={56} />
                <span className="gold-text">GoldWatch</span>
              </p>
            </Reveal>
            <Reveal delay={100}>
              <p className={styles.disclaimer}>ข้อมูลบนเว็บไซต์นี้มีไว้เพื่อติดตามราคาเท่านั้น ไม่ใช่คำแนะนำในการลงทุน</p>
            </Reveal>
          </div>
          <Reveal delay={200} className={styles.sources}>
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
                </a>{" "}
                (สำรอง:{" "}
                <a href="https://www.swissquote.com" target="_blank" rel="noopener noreferrer">
                  Swissquote
                </a>
                )
              </li>
              <li>
                อัตราแลกเปลี่ยน USD/THB:{" "}
                <a href="https://www.exchangerate-api.com" target="_blank" rel="noopener noreferrer">
                  Rates By Exchange Rate API
                </a>{" "}
                (อัปเดตวันละครั้ง)
              </li>
            </ul>
            <p className={styles.note}>ราคาทองไทยอาจช้ากว่าแหล่งต้นทางได้ราว 5 นาที ทองโลกราว 1 นาที และเวลาทั้งหมดแสดงตามเวลาประเทศไทย</p>
            <p className={styles.credit}>
              ภาพประกอบจาก Wikimedia Commons (ปรับสีและตัดขอบภาพ) · ภาพร้านทอง: Adam Jones,{" "}
              <a href="https://creativecommons.org/licenses/by-sa/2.0/" target="_blank" rel="noopener noreferrer">
                CC BY-SA 2.0
              </a>
            </p>
          </Reveal>
        </div>
      </div>
    </footer>
  );
}
