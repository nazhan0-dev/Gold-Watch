package com.goldwatch.goldtracker.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Supported history windows. Bucket boundaries are aligned to Thai local time so that "a day" means a Thai
 * calendar day:
 * <ul>
 *   <li>1D: 24 hourly buckets (current hour included)</li>
 *   <li>1W: 7 daily buckets (today included)</li>
 *   <li>1M: 30 daily buckets</li>
 *   <li>1Y: 52 weekly buckets, weeks start on Monday</li>
 * </ul>
 */
public enum HistoryRange {
    ONE_DAY("1D", Granularity.HOUR),
    ONE_WEEK("1W", Granularity.DAY),
    ONE_MONTH("1M", Granularity.DAY),
    ONE_YEAR("1Y", Granularity.WEEK);

    public enum Granularity {
        HOUR, DAY, WEEK
    }

    private final String code;
    private final Granularity granularity;

    HistoryRange(String code, Granularity granularity) {
        this.code = code;
        this.granularity = granularity;
    }

    public String code() {
        return code;
    }

    public Granularity granularity() {
        return granularity;
    }

    /** Inclusive start of the window, aligned to a bucket boundary in {@code zone}. */
    public Instant windowStart(Instant now, ZoneId zone) {
        ZonedDateTime local = now.atZone(zone);
        LocalDate today = local.toLocalDate();
        return switch (this) {
            case ONE_DAY -> local.truncatedTo(ChronoUnit.HOURS).minusHours(23).toInstant();
            case ONE_WEEK -> today.minusDays(6).atStartOfDay(zone).toInstant();
            case ONE_MONTH -> today.minusDays(29).atStartOfDay(zone).toInstant();
            case ONE_YEAR -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .minusWeeks(51).atStartOfDay(zone).toInstant();
        };
    }

    public static HistoryRange fromCode(String code) {
        String normalized = code == null ? "" : code.strip().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(r -> r.code.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown range '" + code + "'. Allowed: " + allowedCodes()));
    }

    public static String allowedCodes() {
        return Arrays.stream(values()).map(HistoryRange::code).collect(Collectors.joining(", "));
    }
}
