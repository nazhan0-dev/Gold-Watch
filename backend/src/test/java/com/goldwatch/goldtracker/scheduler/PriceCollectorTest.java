package com.goldwatch.goldtracker.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goldwatch.goldtracker.service.FxRateCache;
import com.goldwatch.goldtracker.service.PriceIngestionService;
import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.SpotQuote;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriceCollectorTest {

    private static final Instant NOW = Instant.parse("2026-11-22T07:00:00Z");

    @SuppressWarnings("unchecked")
    private final PriceSource<ThaiGoldQuote> thaiGold = mock(PriceSource.class);
    @SuppressWarnings("unchecked")
    private final PriceSource<SpotQuote> spot = mock(PriceSource.class);
    private final FxRateCache fx = mock(FxRateCache.class);
    private final PriceIngestionService ingestion = mock(PriceIngestionService.class);
    private final PriceCollector collector = new PriceCollector(thaiGold, spot, fx, ingestion);

    @BeforeEach
    void names() {
        when(thaiGold.name()).thenReturn("goldtraders.or.th");
        when(spot.name()).thenReturn("gold-api.com");
        when(fx.sourceName()).thenReturn("open.er-api.com");
    }

    @Test
    void failingThaiSourceWritesNothingAndDoesNotStopSpot() {
        when(thaiGold.fetch()).thenThrow(new PriceSourceException("structure changed"));
        SpotQuote quote = new SpotQuote(new BigDecimal("3539"), NOW);
        FxQuote rate = new FxQuote(new BigDecimal("33.9"), NOW);
        when(spot.fetch()).thenReturn(quote);
        when(fx.currentRate()).thenReturn(rate);

        assertThat(collector.collectThaiGold()).isFalse();
        assertThat(collector.collectSpot()).isTrue();

        verify(ingestion, never()).saveThaiGold(any(), anyString());
        verify(ingestion).saveSpot(quote, rate, "gold-api.com,open.er-api.com");
    }

    @Test
    void failingSpotSourceWritesNothing() {
        when(spot.fetch()).thenThrow(new PriceSourceException("timeout"));

        assertThat(collector.collectSpot()).isFalse();

        verify(ingestion, never()).saveSpot(any(), any(), anyString());
    }

    @Test
    void spotWithoutFxNamesOnlySpotSource() {
        SpotQuote quote = new SpotQuote(new BigDecimal("3539"), NOW);
        when(spot.fetch()).thenReturn(quote);
        when(fx.currentRate()).thenReturn(null);

        collector.collectSpot();

        verify(ingestion).saveSpot(eq(quote), eq(null), eq("gold-api.com"));
    }

    @Test
    void unexpectedErrorsAreContained() {
        when(thaiGold.fetch()).thenThrow(new IllegalStateException("bug"));

        assertThat(collector.collectThaiGold()).isFalse();
    }
}
