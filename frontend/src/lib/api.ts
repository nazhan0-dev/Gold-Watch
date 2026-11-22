/** Types and fetchers for the GoldWatch REST API. Prices arrive as exact decimal strings. */

export type ThaiGoldLatest = {
  buy: string;
  sell: string;
  previousClose: string | null;
  change: string | null;
  changePercent: string | null;
  updatedAt: string;
};

export type QuoteLatest = {
  price: string;
  unit: string;
  previousClose: string | null;
  change: string | null;
  changePercent: string | null;
  updatedAt: string;
};

export type LatestPrices = {
  bar: ThaiGoldLatest | null;
  ornament: ThaiGoldLatest | null;
  spot: QuoteLatest | null;
  usdThb: QuoteLatest | null;
  updatedAt: string | null;
};

export type SeriesType = "bar" | "ornament" | "spot";
export type HistoryRange = "1D" | "1W" | "1M" | "1Y";
export type HistoryInterval = "hour" | "day" | "week";

export type HistoryPoint = {
  time: string;
  open: string;
  high: string;
  low: string;
  close: string;
};

export type HistoryStats = {
  open: string;
  close: string;
  high: string;
  low: string;
  change: string;
  changePercent: string | null;
};

export type History = {
  type: SeriesType;
  range: HistoryRange;
  interval: HistoryInterval;
  unit: "THB" | "USD";
  from: string;
  to: string;
  points: HistoryPoint[];
  stats: HistoryStats | null;
};

export type AnnouncementRound = {
  round: number;
  announcedAt: string;
  barBuy: string | null;
  barSell: string | null;
  ornamentBuy: string | null;
  ornamentSell: string | null;
  change: string | null;
};

export type Announcements = {
  date: string;
  rounds: AnnouncementRound[];
};

export const API_BASE_URL = (process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "");

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status?: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function getJson<T>(path: string, signal?: AbortSignal): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { signal, headers: { Accept: "application/json" } });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") throw error;
    throw new ApiError("ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้");
  }
  if (!response.ok) {
    throw new ApiError(`เซิร์ฟเวอร์ตอบกลับผิดพลาด (${response.status})`, response.status);
  }
  return (await response.json()) as T;
}

export const fetchLatest = (signal?: AbortSignal) => getJson<LatestPrices>("/api/prices/latest", signal);

export const fetchHistory = (type: SeriesType, range: HistoryRange, signal?: AbortSignal) =>
  getJson<History>(`/api/prices/history?type=${type}&range=${range}`, signal);

export const fetchAnnouncements = (signal?: AbortSignal) =>
  getJson<Announcements>("/api/prices/announcements", signal);
