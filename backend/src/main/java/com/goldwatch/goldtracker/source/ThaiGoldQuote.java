package com.goldwatch.goldtracker.source;

import java.math.BigDecimal;
import java.time.Instant;

/** One announcement round of Thai 96.5% gold prices (THB per baht-weight). */
public record ThaiGoldQuote(
        Instant announcedAt,
        int round,
        BigDecimal barBuy,
        BigDecimal barSell,
        BigDecimal ornamentBuy,
        BigDecimal ornamentSell) {
}
