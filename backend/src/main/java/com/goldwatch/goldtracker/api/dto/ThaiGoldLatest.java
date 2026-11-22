package com.goldwatch.goldtracker.api.dto;

import java.time.Instant;

/**
 * Latest Thai gold price for one type. Prices are strings to keep exact decimal precision.
 * Change values compare the selling price with the last price of the previous Thai calendar day
 * and are null when there is no earlier day yet.
 */
public record ThaiGoldLatest(
        String buy,
        String sell,
        String previousClose,
        String change,
        String changePercent,
        Instant updatedAt) {
}
