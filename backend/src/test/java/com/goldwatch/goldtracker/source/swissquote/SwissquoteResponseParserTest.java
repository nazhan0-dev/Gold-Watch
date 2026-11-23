package com.goldwatch.goldtracker.source.swissquote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.SpotQuote;
import com.goldwatch.goldtracker.support.Fixtures;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SwissquoteResponseParserTest {

    private final SwissquoteResponseParser parser = new SwissquoteResponseParser();

    @Test
    void parsesRealResponseAsMidOfFirstSpreadProfile() {
        SpotQuote quote = parser.parse(Fixtures.read("swissquote-xau-usd.json"));

        // (3621.421 + 3622.079) / 2
        assertThat(quote.xauUsd().toPlainString()).isEqualTo("3621.7500");
        assertThat(quote.quotedAt()).isEqualTo(Instant.ofEpochMilli(1795434341738L));
    }

    @Test
    void prefersSwissquoteCapitalMarketsPlatform() {
        SpotQuote quote = parser.parse("""
                [{"topo":{"platform":"AT"},"spreadProfilePrices":[{"bid":3000.0,"ask":3002.0}],"ts":1},
                 {"topo":{"platform":"SwissquoteCapitalMarkets"},"spreadProfilePrices":[{"bid":3600.0,"ask":3601.0}],"ts":2}]
                """);

        assertThat(quote.xauUsd()).isEqualByComparingTo("3600.5");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[]",
            "{}",
            "<html>502</html>",
            "[{\"topo\":{},\"ts\":1}]",
            "[{\"spreadProfilePrices\":[{\"bid\":\"n/a\",\"ask\":3601.0}],\"ts\":1}]",
            "[{\"spreadProfilePrices\":[{\"bid\":3602.0,\"ask\":3601.0}],\"ts\":1}]",
            "[{\"spreadProfilePrices\":[{\"bid\":1.0,\"ask\":1.1}],\"ts\":1}]",
            "[{\"spreadProfilePrices\":[{\"bid\":3600.0,\"ask\":3601.0}]}]",
    })
    void rejectsBrokenResponses(String body) {
        assertThatThrownBy(() -> parser.parse(body)).isInstanceOf(PriceSourceException.class);
    }
}
