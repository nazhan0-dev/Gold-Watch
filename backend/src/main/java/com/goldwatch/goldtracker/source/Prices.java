package com.goldwatch.goldtracker.source;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Small helpers shared by the parsers. Always BigDecimal, never floating point. */
public final class Prices {

    /** {@code 56,950.00}, {@code 56950.00} or {@code 56950}. */
    private static final Pattern GROUPED_NUMBER = Pattern.compile("\\d{1,3}(,\\d{3})*(\\.\\d+)?|\\d+(\\.\\d+)?");

    private Prices() {
    }

    /** Parses numbers such as {@code 56,950.00}; rejects anything else instead of guessing. */
    public static BigDecimal parseGrouped(String raw, String field) {
        String text = raw == null ? "" : raw.strip();
        if (text.isEmpty()) {
            throw new PriceSourceException(field + " is empty");
        }
        if (!GROUPED_NUMBER.matcher(text).matches()) {
            throw new PriceSourceException(field + " is not a number: \"" + text + "\"");
        }
        return new BigDecimal(text.replace(",", ""));
    }

    /** Ensures {@code min <= value <= max}; protects the database from obviously wrong values. */
    public static BigDecimal requireInRange(BigDecimal value, String field, String min, String max) {
        if (value == null) {
            throw new PriceSourceException(field + " is missing");
        }
        if (value.compareTo(new BigDecimal(min)) < 0 || value.compareTo(new BigDecimal(max)) > 0) {
            throw new PriceSourceException(field + " out of plausible range [" + min + ", " + max + "]: " + value);
        }
        return value;
    }
}
