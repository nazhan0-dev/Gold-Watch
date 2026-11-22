package com.goldwatch.goldtracker.api.dto;

import java.time.Instant;

/**
 * One announcement round of the day. {@code round} counts from 1 (first round of the Thai day).
 * {@code change} is the bar selling-price change versus the previous round (for round 1: the last round of an
 * earlier day), null when unknown.
 */
public record AnnouncementRound(
        int round,
        Instant announcedAt,
        String barBuy,
        String barSell,
        String ornamentBuy,
        String ornamentSell,
        String change) {
}
