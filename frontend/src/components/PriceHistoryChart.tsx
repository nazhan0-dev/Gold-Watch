"use client";

import { useCallback, useState, type CSSProperties } from "react";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { fetchHistory, type History, type HistoryInterval, type HistoryRange, type SeriesType } from "@/lib/api";
import { formatAxisTick, formatBucketLabel, formatNumber, formatThaiDateTime } from "@/lib/format";
import { usePolling } from "@/hooks/usePolling";
import { AnimatedValue } from "./AnimatedValue";
import { ChangeIndicator } from "./ChangeIndicator";
import { AlertIcon } from "./Icons";
import { Reveal } from "./Reveal";
import styles from "./PriceHistoryChart.module.css";

const TYPES: { value: SeriesType; label: string }[] = [
  { value: "bar", label: "ทองคำแท่ง" },
  { value: "ornament", label: "ทองรูปพรรณ" },
  { value: "spot", label: "ทองโลก" },
];

const RANGES: { value: HistoryRange; label: string }[] = [
  { value: "1D", label: "1 วัน" },
  { value: "1W", label: "1 สัปดาห์" },
  { value: "1M", label: "1 เดือน" },
  { value: "1Y", label: "1 ปี" },
];

const REFRESH_MS = 5 * 60_000;

const unitLabel = (unit: History["unit"] | undefined) => (unit === "USD" ? "ดอลลาร์/ทรอยออนซ์" : "บาท");

const prefersReducedMotion = () =>
  typeof window !== "undefined" && window.matchMedia?.("(prefers-reduced-motion: reduce)").matches === true;

type Option<T extends string> = { value: T; label: string };

function Segmented<T extends string>({
  label,
  options,
  value,
  onChange,
}: {
  label: string;
  options: Option<T>[];
  value: T;
  onChange: (value: T) => void;
}) {
  const index = Math.max(
    0,
    options.findIndex((option) => option.value === value),
  );
  return (
    <div
      className={styles.segmented}
      role="group"
      aria-label={label}
      // the gold pill slides to the pressed button
      style={{ "--count": options.length, "--index": index } as CSSProperties}
    >
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          className={styles.segment}
          aria-pressed={option.value === value}
          onClick={() => onChange(option.value)}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}

type ChartDatum = { time: string; close: number; closeText: string };

function ChartTooltip({
  active,
  payload,
  interval,
  unit,
}: {
  active?: boolean;
  payload?: ReadonlyArray<{ payload: ChartDatum }>;
  interval: HistoryInterval;
  unit: string;
}) {
  if (!active || !payload?.length) return null;
  const datum = payload[0].payload;
  return (
    <div className={styles.tooltip}>
      <p className={styles.tooltipTime}>{formatBucketLabel(datum.time, interval)}</p>
      <p className={`${styles.tooltipPrice} num`}>
        {formatNumber(datum.closeText)} <span>{unit}</span>
      </p>
    </div>
  );
}

export function PriceHistoryChart() {
  const [type, setType] = useState<SeriesType>("bar");
  const [range, setRange] = useState<HistoryRange>("1D");

  const fetcher = useCallback((signal: AbortSignal) => fetchHistory(type, range, signal), [type, range]);
  const { data, loading, error } = usePolling(fetcher, REFRESH_MS, `${type}-${range}`);

  const typeLabel = TYPES.find((t) => t.value === type)?.label ?? "";
  const unit = unitLabel(data?.unit);
  const latest = data?.points.at(-1);
  const chartData: ChartDatum[] = (data?.points ?? []).map((p) => ({
    time: p.time,
    close: Number(p.close),
    closeText: p.close,
  }));
  const fractionDigits = 2;

  return (
    <section id="history" className={styles.section} aria-labelledby="history-title">
      <Reveal className={styles.panel}>
        <span className={styles.panelGlow} aria-hidden="true" />
        <header className={styles.header}>
          <div>
            <p className={styles.kicker} aria-hidden="true">
              <span className={styles.diamond} />
              History
            </p>
            <h2 id="history-title" className={styles.title}>
              กราฟราคาย้อนหลัง
            </h2>
            <p className={`${styles.subtitle} num`} aria-live="polite">
              {typeLabel}
              {latest ? ` · ล่าสุด ${formatNumber(latest.close)} ${unit}` : ""}
            </p>
          </div>
          <div className={styles.controls}>
            <Segmented label="ประเภทราคา" options={TYPES} value={type} onChange={setType} />
            <Segmented label="ช่วงเวลา" options={RANGES} value={range} onChange={setRange} />
          </div>
        </header>

        <dl className={styles.stats}>
          <div>
            <dt>สูงสุด</dt>
            <dd className="num">
              <AnimatedValue text={formatNumber(data?.stats?.high, fractionDigits)} />
            </dd>
          </div>
          <div>
            <dt>ต่ำสุด</dt>
            <dd className="num">
              <AnimatedValue text={formatNumber(data?.stats?.low, fractionDigits)} />
            </dd>
          </div>
          <div>
            <dt>เปลี่ยนแปลงในช่วงนี้</dt>
            <dd>
              {data?.stats ? (
                <ChangeIndicator change={data.stats.change} changePercent={data.stats.changePercent} />
              ) : (
                <span className="num">–</span>
              )}
            </dd>
          </div>
        </dl>

        {error && !data ? (
          <p className={styles.notice} role="status">
            <AlertIcon /> โหลดข้อมูลกราฟไม่สำเร็จ กำลังลองใหม่อัตโนมัติ
          </p>
        ) : null}

        <div className={styles.chartArea}>
          {loading ? (
            <div className={styles.skeleton} aria-busy="true">
              <span className="visually-hidden">กำลังโหลดกราฟ</span>
            </div>
          ) : chartData.length < 2 ? (
            <div className={styles.empty}>
              <svg className={styles.emptyLine} viewBox="0 0 400 80" preserveAspectRatio="none" aria-hidden="true">
                <path d="M0 58 C 50 52, 80 22, 140 34 S 240 72, 300 42 S 370 16, 400 24" />
              </svg>
              <span className={styles.emptyMark} aria-hidden="true" />
              <p className={styles.emptyTitle}>ข้อมูลยังสะสมไม่พอสำหรับช่วงนี้</p>
              <p className={styles.emptyHint}>ระบบเก็บราคาทุก 5 นาที ลองเลือกช่วงเวลาอื่นได้เลย</p>
            </div>
          ) : (
            <figure
              key={`${type}-${range}`}
              className={styles.figure}
              role="img"
              aria-label={`กราฟราคา${typeLabel} ${chartData.length} จุด ตั้งแต่ ${formatThaiDateTime(
                chartData[0].time,
              )} ถึง ${formatThaiDateTime(chartData.at(-1)!.time)}`}
            >
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 8, bottom: 0, left: 8 }}>
                  <defs>
                    <linearGradient id="goldFill" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="var(--color-gold-light)" stopOpacity={0.38} />
                      <stop offset="55%" stopColor="var(--color-gold)" stopOpacity={0.12} />
                      <stop offset="100%" stopColor="var(--color-gold)" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="goldStroke" x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0%" stopColor="var(--color-gold-deep)" />
                      <stop offset="50%" stopColor="var(--color-gold)" />
                      <stop offset="100%" stopColor="var(--color-gold-light)" />
                    </linearGradient>
                  </defs>
                  <CartesianGrid vertical={false} stroke="rgba(201,164,92,0.1)" strokeDasharray="2 6" />
                  <XAxis
                    dataKey="time"
                    tickFormatter={(value: string) => formatAxisTick(value, data!.interval)}
                    tick={{ fill: "var(--color-text-faint)", fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                    minTickGap={24}
                  />
                  <YAxis
                    domain={[(min: number) => min - (min * 0.002 || 1), (max: number) => max + (max * 0.002 || 1)]}
                    tickFormatter={(value: number) =>
                      value.toLocaleString("en-US", {
                        maximumFractionDigits: 0,
                      })
                    }
                    tick={{ fill: "var(--color-text-faint)", fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                    width={64}
                  />
                  <Tooltip
                    content={(props) => (
                      <ChartTooltip
                        active={props.active}
                        payload={
                          props.payload as unknown as ReadonlyArray<{
                            payload: ChartDatum;
                          }>
                        }
                        interval={data!.interval}
                        unit={unit}
                      />
                    )}
                    cursor={{
                      stroke: "var(--color-gold-light)",
                      strokeOpacity: 0.45,
                      strokeDasharray: "3 4",
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="close"
                    stroke="url(#goldStroke)"
                    strokeWidth={2.5}
                    fill="url(#goldFill)"
                    activeDot={{
                      r: 6,
                      fill: "var(--color-gold-light)",
                      stroke: "var(--color-black)",
                      strokeWidth: 3,
                    }}
                    isAnimationActive={!prefersReducedMotion()}
                    animationDuration={1600}
                    animationEasing="ease-out"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </figure>
          )}
        </div>
      </Reveal>
    </section>
  );
}
