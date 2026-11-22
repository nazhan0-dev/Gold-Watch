package com.goldwatch.goldtracker.api.dto;

import java.time.Instant;

/** One aggregated bucket; {@code time} is the start of the bucket (UTC). */
public record HistoryPoint(Instant time, String open, String high, String low, String close) {
}
