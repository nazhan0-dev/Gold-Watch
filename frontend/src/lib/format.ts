/** Display formatting. All times are shown in Thai time (Asia/Bangkok) whatever the visitor's zone is. */

import type { HistoryInterval } from "./api";
import type { Direction } from "./change";

const TIME_ZONE = "Asia/Bangkok";
const PLACEHOLDER = "–";
const MINUS = "−"; // typographic minus, same width as "+"

/** "57150.00" -> "57,150.00". Grouping is applied to the exact string, so no precision is lost. */
export function formatNumber(value: string | null | undefined, fractionDigits = 2): string {
  if (value == null || value === "") return PLACEHOLDER;
  const match = /^([+-])?(\d+)(?:\.(\d+))?$/.exec(value.trim());
  if (!match) return PLACEHOLDER;
  const [, sign, whole, fraction = ""] = match;
  let digits = fraction;
  let integer = whole;
  if (digits.length > fractionDigits) {
    // round half-up on the decimal string
    const scaled = BigInt(whole + digits.slice(0, fractionDigits));
    const roundUp = Number(digits[fractionDigits]) >= 5;
    const rounded = (scaled + (roundUp ? 1n : 0n)).toString().padStart(fractionDigits + 1, "0");
    integer = fractionDigits === 0 ? rounded : rounded.slice(0, -fractionDigits);
    digits = fractionDigits === 0 ? "" : rounded.slice(-fractionDigits);
  } else {
    digits = digits.padEnd(fractionDigits, "0");
  }
  const grouped = integer.replace(/^0+(?=\d)/, "").replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  const isZero = /^[0,]*$/.test(grouped) && /^0*$/.test(digits);
  const prefix = sign === "-" && !isZero ? MINUS : "";
  return `${prefix}${grouped}${digits ? `.${digits}` : ""}`;
}

/** "+150.00" / "−150.00" / "0.00" — the sign is always visible, never colour alone. */
export function formatSignedNumber(value: string | null | undefined, fractionDigits = 2): string {
  if (value == null) return PLACEHOLDER;
  const formatted = formatNumber(value, fractionDigits);
  if (formatted === PLACEHOLDER) return formatted;
  if (formatted.startsWith(MINUS) || /^[0,.]+$/.test(formatted)) return formatted;
  return `+${formatted}`;
}

/** "+0.26%" / "−1.05%" / "0.00%". */
export function formatSignedPercent(value: string | null | undefined): string {
  const formatted = formatSignedNumber(value, 2);
  return formatted === PLACEHOLDER ? formatted : `${formatted}%`;
}

/** ▲ for up, ▼ for down, • for unchanged. */
export function changeSymbol(direction: Direction | null): string {
  if (direction === "up") return "▲";
  if (direction === "down") return "▼";
  return "•";
}

/** Screen-reader wording for a direction. */
export function changeWord(direction: Direction | null): string {
  if (direction === "up") return "เพิ่มขึ้น";
  if (direction === "down") return "ลดลง";
  return "ไม่เปลี่ยนแปลง";
}

const timeFormatter = new Intl.DateTimeFormat("th-TH", {
  timeZone: TIME_ZONE,
  hour: "2-digit",
  minute: "2-digit",
  hourCycle: "h23",
});

const dateFormatter = new Intl.DateTimeFormat("th-TH-u-ca-buddhist", {
  timeZone: TIME_ZONE,
  day: "numeric",
  month: "short",
  year: "numeric",
});

const shortDateFormatter = new Intl.DateTimeFormat("th-TH-u-ca-buddhist", {
  timeZone: TIME_ZONE,
  day: "numeric",
  month: "short",
});

const dateKeyFormatter = new Intl.DateTimeFormat("en-CA", {
  timeZone: TIME_ZONE,
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});

function toDate(iso: string | null | undefined): Date | null {
  if (!iso) return null;
  const date = new Date(iso);
  return Number.isNaN(date.getTime()) ? null : date;
}

/** ISO UTC -> "14:05" in Thai time. */
export function formatThaiTime(iso: string | null | undefined): string {
  const date = toDate(iso);
  return date ? timeFormatter.format(date) : PLACEHOLDER;
}

/** ISO UTC -> "22 พ.ย. 2569" (Buddhist year, as Thai readers expect). */
export function formatThaiDate(iso: string | null | undefined): string {
  const date = toDate(iso);
  return date ? dateFormatter.format(date) : PLACEHOLDER;
}

/** ISO UTC -> "22 พ.ย." (no year, for tight spaces). */
export function formatThaiShortDate(iso: string | null | undefined): string {
  const date = toDate(iso);
  return date ? shortDateFormatter.format(date) : PLACEHOLDER;
}

/** ISO UTC -> "22 พ.ย. 2569 14:05 น." */
export function formatThaiDateTime(iso: string | null | undefined): string {
  const date = toDate(iso);
  return date ? `${dateFormatter.format(date)} ${timeFormatter.format(date)} น.` : PLACEHOLDER;
}

/** ISO UTC -> "2026-11-22", the calendar date in Thailand. */
export function bangkokDateKey(iso: string | null | undefined): string | null {
  const date = toDate(iso);
  return date ? dateKeyFormatter.format(date) : null;
}

/** Chart axis label: hour buckets show the time, day/week buckets show the date. */
export function formatAxisTick(iso: string, interval: HistoryInterval): string {
  const date = toDate(iso);
  if (!date) return "";
  return interval === "hour" ? timeFormatter.format(date) : shortDateFormatter.format(date);
}

/** Tooltip label for a bucket. */
export function formatBucketLabel(iso: string, interval: HistoryInterval): string {
  if (interval === "hour") return formatThaiDateTime(iso);
  if (interval === "week") return `สัปดาห์ที่เริ่ม ${formatThaiDate(iso)}`;
  return formatThaiDate(iso);
}
