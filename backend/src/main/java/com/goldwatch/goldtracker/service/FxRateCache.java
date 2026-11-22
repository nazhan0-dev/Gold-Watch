package com.goldwatch.goldtracker.service;

import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.PriceSourceException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Keeps the last good USD/THB rate in memory. The FX provider only updates daily, so it is asked at most once per
 * {@code min-refresh}; a failed refresh keeps the previous rate until it is older than {@code max-age}.
 */
@Component
public class FxRateCache {

    private static final Logger log = LoggerFactory.getLogger(FxRateCache.class);

    private final PriceSource<FxQuote> source;
    private final PriceIngestionService ingestion;
    private final Clock clock;
    private final Duration minRefresh;
    private final Duration maxAge;

    private FxQuote current;
    private Instant lastAttempt;
    private boolean warmedUp;

    public FxRateCache(PriceSource<FxQuote> source, PriceIngestionService ingestion, Clock clock,
            GoldTrackerProperties properties) {
        this.source = source;
        this.ingestion = ingestion;
        this.clock = clock;
        this.minRefresh = properties.sources().erApi().minRefresh();
        this.maxAge = properties.sources().erApi().maxAge();
    }

    public String sourceName() {
        return source.name();
    }

    /** Returns a usable rate, refreshing from the provider when due; null when none is known or it is too old. */
    public synchronized FxQuote currentRate() {
        Instant now = clock.instant();
        if (!warmedUp) {
            warmedUp = true;
            current = ingestion.latestStoredFx().orElse(null);
        }
        if (lastAttempt == null || !now.isBefore(lastAttempt.plus(minRefresh))) {
            lastAttempt = now;
            try {
                current = source.fetch();
            } catch (PriceSourceException e) {
                log.error("Price source {} failed, keeping previous rate: {}", source.name(), e.getMessage());
            }
        }
        if (current != null && current.quotedAt().plus(maxAge).isBefore(now)) {
            log.warn("USD/THB rate from {} is older than {}; not attaching it", current.quotedAt(), maxAge);
            return null;
        }
        return current;
    }
}
