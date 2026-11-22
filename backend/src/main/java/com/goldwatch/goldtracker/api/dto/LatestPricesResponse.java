package com.goldwatch.goldtracker.api.dto;

import java.time.Instant;

/** Response of GET /api/prices/latest. Any part is null when no data has been collected for it yet. */
public record LatestPricesResponse(
        ThaiGoldLatest bar,
        ThaiGoldLatest ornament,
        QuoteLatest spot,
        QuoteLatest usdThb,
        Instant updatedAt) {
}
