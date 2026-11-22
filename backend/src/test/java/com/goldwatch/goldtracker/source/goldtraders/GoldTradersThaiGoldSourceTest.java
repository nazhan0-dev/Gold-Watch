package com.goldwatch.goldtracker.source.goldtraders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import com.goldwatch.goldtracker.source.HttpFetcher;
import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.support.Fixtures;
import com.goldwatch.goldtracker.support.TestProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class GoldTradersThaiGoldSourceTest {

    private final HttpFetcher http = mock(HttpFetcher.class);
    private final GoldTrackerProperties properties = TestProperties.defaults();

    @Test
    void fetchesHomePageAndParsesIt() {
        when(http.get(eq("https://www.goldtraders.or.th/"), any())).thenReturn(Fixtures.read("goldtraders-home.html"));
        Clock clock = Clock.fixed(Instant.parse("2026-11-22T03:00:00Z"), ZoneOffset.UTC);

        var source = new GoldTradersThaiGoldSource(http, properties, clock);

        assertThat(source.fetch().barSell()).isEqualByComparingTo("57150");
        assertThat(source.name()).isEqualTo("goldtraders.or.th");
    }

    @Test
    void rejectsAnnouncementInTheFuture() {
        when(http.get(any(), any())).thenReturn(Fixtures.read("goldtraders-home.html"));
        Clock dayBefore = Clock.fixed(Instant.parse("2026-11-21T03:00:00Z"), ZoneOffset.UTC);

        var source = new GoldTradersThaiGoldSource(http, properties, dayBefore);

        assertThatThrownBy(source::fetch).isInstanceOf(PriceSourceException.class).hasMessageContaining("future");
    }
}
