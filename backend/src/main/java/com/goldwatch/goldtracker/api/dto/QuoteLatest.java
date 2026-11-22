package com.goldwatch.goldtracker.api.dto;

import java.time.Instant;

/** Latest value of a single-price quote (XAU/USD or USD/THB) with the change versus the previous day close. */
public record QuoteLatest(
        String price,
        String unit,
        String previousClose,
        String change,
        String changePercent,
        Instant updatedAt) {
}
