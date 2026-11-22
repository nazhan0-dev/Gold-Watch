package com.goldwatch.goldtracker.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** A chartable price series. Bar and ornament use the selling price; spot uses XAU/USD. */
public enum PriceSeries {
    BAR("bar", "THB", GoldType.BAR),
    ORNAMENT("ornament", "THB", GoldType.ORNAMENT),
    SPOT("spot", "USD", null);

    private final String code;
    private final String unit;
    private final GoldType goldType;

    PriceSeries(String code, String unit, GoldType goldType) {
        this.code = code;
        this.unit = unit;
        this.goldType = goldType;
    }

    public String code() {
        return code;
    }

    public String unit() {
        return unit;
    }

    /** Thai gold type for bar/ornament; null for spot. */
    public GoldType goldType() {
        return goldType;
    }

    public static PriceSeries fromCode(String code) {
        String normalized = code == null ? "" : code.strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(s -> s.code.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown type '" + code + "'. Allowed: " + allowedCodes()));
    }

    public static String allowedCodes() {
        return Arrays.stream(values()).map(PriceSeries::code).collect(Collectors.joining(", "));
    }
}
