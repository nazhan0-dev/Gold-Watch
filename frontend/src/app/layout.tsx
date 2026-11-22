import type { Metadata, Viewport } from "next";
import { Cinzel, Cormorant_Garamond, IBM_Plex_Sans_Thai_Looped, Taviraj } from "next/font/google";
import { THEME_INIT_SCRIPT } from "@/lib/theme";
import "./globals.css";

/** High-contrast luxury serif for Latin letters and the big numbers. */
const cormorant = Cormorant_Garamond({
  weight: ["500", "600", "700"],
  subsets: ["latin"],
  variable: "--font-cormorant",
  display: "swap",
});

/** Elegant looped Thai serif; picks up the Thai glyphs Cormorant lacks. */
const taviraj = Taviraj({
  weight: ["300", "400", "500", "600"],
  subsets: ["thai"],
  variable: "--font-taviraj",
  display: "swap",
});

/** Engraved-capitals face for the wordmark and small Latin labels. */
const cinzel = Cinzel({
  weight: "variable",
  subsets: ["latin"],
  variable: "--font-cinzel",
  display: "swap",
});

/** Body text: looped Thai sans, more formal than loopless and in tune with the serif. */
const plexThai = IBM_Plex_Sans_Thai_Looped({
  weight: ["300", "400", "500"],
  subsets: ["thai", "latin"],
  variable: "--font-plex-thai",
  display: "swap",
});

export const metadata: Metadata = {
  title: "GoldWatch — ราคาทองคำวันนี้",
  description: "ติดตามราคาทองคำไทย (ทองคำแท่ง ทองรูปพรรณ 96.5%) และราคาทองโลก พร้อมกราฟย้อนหลังและประวัติการประกาศ",
};

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#faf7f0" },
    { media: "(prefers-color-scheme: dark)", color: "#08080a" },
  ],
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    // data-theme is set by the inline script before paint, so React must not complain about it
    <html
      lang="th"
      className={`${cormorant.variable} ${taviraj.variable} ${cinzel.variable} ${plexThai.variable}`}
      suppressHydrationWarning
    >
      <head>
        <script dangerouslySetInnerHTML={{ __html: THEME_INIT_SCRIPT }} />
      </head>
      <body>{children}</body>
    </html>
  );
}
