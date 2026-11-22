package com.goldwatch.goldtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.support.TestProperties;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FxRateCacheTest {

    private static final Instant START = Instant.parse("2026-11-22T07:00:00Z");

    @SuppressWarnings("unchecked")
    private final PriceSource<FxQuote> source = mock(PriceSource.class);
    private final PriceIngestionService ingestion = mock(PriceIngestionService.class);
    private final MutableClock clock = new MutableClock(START);
    private FxRateCache cache;

    @BeforeEach
    void setUp() {
        when(ingestion.latestStoredFx()).thenReturn(Optional.empty());
        cache = new FxRateCache(source, ingestion, clock, TestProperties.defaults());
    }

    @Test
    void fetchesAtMostOncePerRefreshInterval() {
        FxQuote rate = new FxQuote(new BigDecimal("33.9"), START);
        when(source.fetch()).thenReturn(rate);

        assertThat(cache.currentRate()).isEqualTo(rate);
        clock.advance(Duration.ofMinutes(30));
        assertThat(cache.currentRate()).isEqualTo(rate);
        verify(source, times(1)).fetch();

        clock.advance(Duration.ofMinutes(31));
        cache.currentRate();
        verify(source, times(2)).fetch();
    }

    @Test
    void keepsPreviousRateWhenProviderFails() {
        FxQuote rate = new FxQuote(new BigDecimal("33.9"), START);
        when(source.fetch()).thenReturn(rate).thenThrow(new PriceSourceException("down"));

        cache.currentRate();
        clock.advance(Duration.ofHours(2));

        assertThat(cache.currentRate()).isEqualTo(rate);
    }

    @Test
    void dropsRateOlderThanMaxAge() {
        when(source.fetch()).thenThrow(new PriceSourceException("down"));
        when(ingestion.latestStoredFx()).thenReturn(Optional.of(
                new FxQuote(new BigDecimal("33.9"), START.minus(Duration.ofDays(3)))));
        cache = new FxRateCache(source, ingestion, clock, TestProperties.defaults());

        assertThat(cache.currentRate()).isNull();
    }

    @Test
    void returnsNullWhenNothingIsKnown() {
        when(source.fetch()).thenThrow(new PriceSourceException("down"));

        assertThat(cache.currentRate()).isNull();
    }

    /** Test clock that can be moved forward. */
    static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
