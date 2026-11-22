import { formatThaiTime } from "@/lib/format";
import { GoldBarIcon } from "./Icons";
import styles from "./Navbar.module.css";

export const NAV_LINKS = [
  { href: "#today", label: "ราคาวันนี้" },
  { href: "#history", label: "กราฟย้อนหลัง" },
  { href: "#announcements", label: "ประวัติการประกาศ" },
] as const;

type Props = { updatedAt: string | null | undefined };

export function Navbar({ updatedAt }: Props) {
  return (
    <header className={styles.navbar}>
      <a href="#top" className={styles.brand} aria-label="GoldWatch หน้าแรก">
        <GoldBarIcon className={styles.logo} width={28} height={28} />
        <span>GoldWatch</span>
      </a>
      <nav aria-label="เมนูหลัก" className={styles.nav}>
        <ul>
          {NAV_LINKS.map((link) => (
            <li key={link.href}>
              <a href={link.href}>{link.label}</a>
            </li>
          ))}
        </ul>
      </nav>
      <p className={styles.status} aria-live="polite">
        <span className={styles.dot} aria-hidden="true" />
        <span className="num">{updatedAt ? `อัปเดตล่าสุด ${formatThaiTime(updatedAt)} น.` : "กำลังโหลดข้อมูล…"}</span>
      </p>
    </header>
  );
}
