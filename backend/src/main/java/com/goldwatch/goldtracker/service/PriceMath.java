package com.goldwatch.goldtracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Price arithmetic and formatting helpers (BigDecimal only). */
public final class PriceMath {

    private PriceMath() {
    }

    /** {@code current - previous}, or null when either side is unknown. */
    public static BigDecimal change(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null) {
            return null;
        }
        return current.subtract(previous);
    }

    /** Percentage change rounded to 2 decimals, or null when it cannot be computed. */
    public static BigDecimal changePercent(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.signum() == 0) {
            return null;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    /** Exact plain-string form for JSON, or null. */
    public static String str(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }
}
