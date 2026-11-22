/**
 * Exact price-change arithmetic on decimal strings (no floating-point drift):
 * "57150.00" - "57000.00" === "150.00", never "149.99999999".
 */

export type Direction = "up" | "down" | "flat";

const DECIMAL = /^([+-])?(\d+)(?:\.(\d+))?$/;

type Scaled = { value: bigint; scale: number };

function toScaled(input: string): Scaled {
  const match = DECIMAL.exec(input.trim());
  if (!match) throw new Error(`Not a decimal number: "${input}"`);
  const [, sign, whole, fraction = ""] = match;
  const value = BigInt(whole + fraction) * (sign === "-" ? -1n : 1n);
  return { value, scale: fraction.length };
}

function rescale(n: Scaled, scale: number): bigint {
  return n.value * 10n ** BigInt(scale - n.scale);
}

function fromScaled(value: bigint, scale: number): string {
  const negative = value < 0n;
  const digits = (negative ? -value : value).toString().padStart(scale + 1, "0");
  const whole = scale === 0 ? digits : digits.slice(0, -scale);
  const fraction = scale === 0 ? "" : `.${digits.slice(-scale)}`;
  return `${negative ? "-" : ""}${whole}${fraction}`;
}

/** Exact `current - previous`, keeping the larger number of decimals; null if either is missing. */
export function calcChange(current: string | null | undefined, previous: string | null | undefined): string | null {
  if (current == null || previous == null) return null;
  const a = toScaled(current);
  const b = toScaled(previous);
  const scale = Math.max(a.scale, b.scale);
  return fromScaled(rescale(a, scale) - rescale(b, scale), scale);
}

/** up / down / flat for a signed decimal string; null when unknown. */
export function changeDirection(change: string | null | undefined): Direction | null {
  if (change == null) return null;
  const { value } = toScaled(change);
  if (value > 0n) return "up";
  if (value < 0n) return "down";
  return "flat";
}
