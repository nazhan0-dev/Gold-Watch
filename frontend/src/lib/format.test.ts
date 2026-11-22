import {
  bangkokDateKey,
  changeSymbol,
  formatAxisTick,
  formatBucketLabel,
  formatNumber,
  formatSignedNumber,
  formatSignedPercent,
  formatThaiDate,
  formatThaiDateTime,
  formatThaiTime,
} from "./format";

const MINUS = "−";

describe("formatNumber", () => {
  it("groups thousands and pads decimals", () => {
    expect(formatNumber("57150.00")).toBe("57,150.00");
    expect(formatNumber("57150")).toBe("57,150.00");
    expect(formatNumber("3539.5000", 2)).toBe("3,539.50");
    expect(formatNumber("1234567.891", 3)).toBe("1,234,567.891");
  });

  it("rounds half-up on the decimal string, including carries", () => {
    expect(formatNumber("33.918378", 2)).toBe("33.92");
    expect(formatNumber("9.995", 2)).toBe("10.00");
    expect(formatNumber("999.5", 0)).toBe("1,000");
  });

  it("uses a typographic minus and never shows -0", () => {
    expect(formatNumber("-150.00")).toBe(`${MINUS}150.00`);
    expect(formatNumber("-0.001")).toBe("0.00");
  });

  it("shows a dash for missing or invalid values", () => {
    expect(formatNumber(null)).toBe("–");
    expect(formatNumber("")).toBe("–");
    expect(formatNumber("abc")).toBe("–");
  });
});

describe("signed values", () => {
  it("always shows the sign so direction is not conveyed by colour alone", () => {
    expect(formatSignedNumber("150")).toBe("+150.00");
    expect(formatSignedNumber("-150")).toBe(`${MINUS}150.00`);
    expect(formatSignedNumber("0.00")).toBe("0.00");
    expect(formatSignedPercent("0.26")).toBe("+0.26%");
    expect(formatSignedPercent("-1.05")).toBe(`${MINUS}1.05%`);
    expect(formatSignedPercent(null)).toBe("–");
  });

  it("maps directions to arrows", () => {
    expect(changeSymbol("up")).toBe("▲");
    expect(changeSymbol("down")).toBe("▼");
    expect(changeSymbol("flat")).toBe("•");
  });
});

describe("Thai time", () => {
  // 2026-11-22T02:09:00Z is 09:09 on 22 Nov 2026 in Bangkok (UTC+7), i.e. 2569 in the Buddhist era.
  const iso = "2026-11-22T02:09:00Z";

  it("converts UTC to Bangkok time with a 24-hour clock", () => {
    expect(formatThaiTime(iso)).toBe("09:09");
    expect(formatThaiTime("2026-11-21T17:30:00Z")).toBe("00:30");
  });

  it("shows Buddhist-era dates", () => {
    expect(formatThaiDate(iso)).toBe("22 พ.ย. 2569");
    expect(formatThaiDateTime(iso)).toBe("22 พ.ย. 2569 09:09 น.");
  });

  it("finds the Thai calendar date across midnight UTC", () => {
    expect(bangkokDateKey("2026-11-21T17:00:00Z")).toBe("2026-11-22");
    expect(bangkokDateKey("2026-11-21T16:59:59Z")).toBe("2026-11-21");
  });

  it("formats axis ticks and tooltip labels per interval", () => {
    expect(formatAxisTick(iso, "hour")).toBe("09:09");
    expect(formatAxisTick("2026-11-21T17:00:00Z", "day")).toBe("22 พ.ย.");
    expect(formatBucketLabel("2026-11-15T17:00:00Z", "week")).toBe("สัปดาห์เริ่ม 16 พ.ย. 2569");
  });

  it("tolerates bad input", () => {
    expect(formatThaiTime(null)).toBe("–");
    expect(formatThaiTime("not a date")).toBe("–");
    expect(bangkokDateKey(undefined)).toBeNull();
  });
});
