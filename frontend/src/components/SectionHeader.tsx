import type { ReactNode } from "react";
import { Reveal } from "./Reveal";
import styles from "./Section.module.css";

type Props = {
  id: string;
  /** Small engraved Latin label above the title, e.g. "Today". */
  kicker: string;
  title: ReactNode;
  subtitle?: ReactNode;
  description: ReactNode;
};

/** Ornamented section heading shared by the content sections; reveals on scroll. */
export function SectionHeader({ id, kicker, title, subtitle, description }: Props) {
  return (
    <header className={styles.header}>
      <Reveal>
        <p className={styles.kicker} aria-hidden="true">
          <span className={styles.diamond} />
          {kicker}
        </p>
        <h2 id={id} className={styles.title}>
          {title}
          {subtitle ? <span className={styles.subtitle}>{subtitle}</span> : null}
        </h2>
      </Reveal>
      <Reveal delay={120}>
        <p className={styles.description}>{description}</p>
      </Reveal>
    </header>
  );
}
