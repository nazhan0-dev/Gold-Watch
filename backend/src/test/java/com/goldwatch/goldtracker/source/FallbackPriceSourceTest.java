package com.goldwatch.goldtracker.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FallbackPriceSourceTest {

    private static final SpotQuote QUOTE = new SpotQuote(new BigDecimal("3600"), Instant.EPOCH);

    private static PriceSource<SpotQuote> ok(String name, SpotQuote quote) {
        return new PriceSource<>() {
            public String name() {
                return name;
            }

            public SpotQuote fetch() {
                return quote;
            }
        };
    }

    private static PriceSource<SpotQuote> failing(String name, String reason) {
        return new PriceSource<>() {
            public String name() {
                return name;
            }

            public SpotQuote fetch() {
                throw new PriceSourceException(reason);
            }
        };
    }

    @Test
    void usesPrimaryWhenItWorks() {
        var source = new FallbackPriceSource<>(ok("primary", QUOTE), failing("backup", "unused"));

        assertThat(source.fetch()).isSameAs(QUOTE);
        assertThat(source.name()).isEqualTo("primary");
    }

    @Test
    void fallsBackAndReportsTheSourceThatAnswered() {
        var source = new FallbackPriceSource<>(failing("primary", "dns"), ok("backup", QUOTE));

        assertThat(source.fetch()).isSameAs(QUOTE);
        assertThat(source.name()).isEqualTo("backup");
    }

    @Test
    void failsWithAllReasonsWhenEverySourceFails() {
        var source = new FallbackPriceSource<>(failing("primary", "dns"), failing("backup", "timeout"));

        assertThatThrownBy(source::fetch)
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("primary: dns")
                .hasMessageContaining("backup: timeout");
        assertThat(source.name()).isEqualTo("primary|backup");
    }

    @Test
    void treatsNullQuoteAsFailure() {
        var source = new FallbackPriceSource<>(ok("primary", null), ok("backup", QUOTE));

        assertThat(source.fetch()).isSameAs(QUOTE);
        assertThat(source.name()).isEqualTo("backup");
    }
}
