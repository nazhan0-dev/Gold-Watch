package com.goldwatch.goldtracker.source;

import java.math.BigDecimal;
import java.time.Instant;

/** THB per 1 USD as published by the provider at quotedAt. */
public record FxQuote(BigDecimal usdThb, Instant quotedAt) {
}
