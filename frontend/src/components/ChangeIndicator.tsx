import { changeDirection } from "@/lib/change";
import { changeSymbol, changeWord, formatSignedNumber, formatSignedPercent } from "@/lib/format";
import styles from "./ChangeIndicator.module.css";

type Props = {
  change: string | null | undefined;
  changePercent?: string | null;
  /** Colour set: "light" backgrounds use green/red, "dark" uses light gold / light red. */
  tone?: "light" | "dark";
  /** Show as a rounded chip. */
  chip?: boolean;
  fractionDigits?: number;
};

/** ▲ +150.00 (+0.26%) — arrow and sign carry the meaning, colour only reinforces it. */
export function ChangeIndicator({ change, changePercent, tone = "light", chip = false, fractionDigits = 2 }: Props) {
  if (change == null) {
    return <span className={`${styles.change} ${styles.unknown} num`}>ยังไม่มีข้อมูลเทียบ</span>;
  }
  const direction = changeDirection(change);
  const className = [styles.change, styles[tone], styles[direction ?? "flat"], chip ? styles.chip : ""].join(" ");
  return (
    <span className={`${className} num`}>
      <span aria-hidden="true">{changeSymbol(direction)}</span>
      <span className="visually-hidden">{changeWord(direction)}</span>
      <span>{formatSignedNumber(change, fractionDigits)}</span>
      {changePercent != null ? <span>({formatSignedPercent(changePercent)})</span> : null}
    </span>
  );
}
