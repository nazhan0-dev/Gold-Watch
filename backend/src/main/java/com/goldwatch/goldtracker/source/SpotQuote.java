package com.goldwatch.goldtracker.source;

import java.math.BigDecimal;
import java.time.Instant;

/** World gold spot price in USD per troy ounce. */
public record SpotQuote(BigDecimal xauUsd, Instant quotedAt) {
}
