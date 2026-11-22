import { changeDirection } from "@/lib/change";
import { changeSymbol, changeWord, formatSignedNumber, formatSignedPercent } from "@/lib/format";
import styles from "./ChangeIndicator.module.css";

type Props = {
  change: string | null | undefined;
  changePercent?: string | null;
  /** Show as a rounded chip. */
  chip?: boolean;
  fractionDigits?: number;
};

/**
 * ▲ +150.00 (+0.26%) — arrow and sign carry the meaning, colour only reinforces it.
 * Up/down colours come from the theme tokens, so it works on both themes.
 */
export function ChangeIndicator({ change, changePercent, chip = false, fractionDigits = 2 }: Props) {
  if (change == null) {
    return <span className={`${styles.change} ${styles.unknown} num`}>ยังไม่มีข้อมูลเทียบ</span>;
  }
  const direction = changeDirection(change) ?? "flat";
  const className = [styles.change, styles[direction], chip ? styles.chip : ""].join(" ");
  return (
    <span className={`${className} num`}>
      <span aria-hidden="true" className={styles.arrow}>
        {changeSymbol(direction)}
      </span>
      <span className="visually-hidden">{changeWord(direction)}</span>
      <span>{formatSignedNumber(change, fractionDigits)}</span>
      {changePercent != null ? <span>({formatSignedPercent(changePercent)})</span> : null}
    </span>
  );
}
