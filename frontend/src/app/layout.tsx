import type { Metadata, Viewport } from "next";
import { Bai_Jamjuree, IBM_Plex_Sans_Thai } from "next/font/google";
import "./globals.css";

const baiJamjuree = Bai_Jamjuree({
  weight: "600",
  subsets: ["thai", "latin"],
  variable: "--font-bai-jamjuree",
  display: "swap",
});

const plexThai = IBM_Plex_Sans_Thai({
  weight: ["400", "500"],
  subsets: ["thai", "latin"],
  variable: "--font-plex-thai",
  display: "swap",
});

export const metadata: Metadata = {
  title: "GoldWatch — ราคาทองคำวันนี้",
  description: "ติดตามราคาทองคำไทย (ทองคำแท่ง ทองรูปพรรณ 96.5%) และราคาทองโลก พร้อมกราฟย้อนหลังและประวัติการประกาศ",
};

export const viewport: Viewport = {
  themeColor: "#0b0b0c",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="th" className={`${baiJamjuree.variable} ${plexThai.variable}`}>
      <body>{children}</body>
    </html>
  );
}
