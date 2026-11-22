package com.goldwatch.goldtracker.source.erapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.support.Fixtures;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ErApiResponseParserTest {

    private final ErApiResponseParser parser = new ErApiResponseParser();

    @Test
    void parsesRealResponse() {
        FxQuote quote = parser.parse(Fixtures.read("er-api-usd.json"));

        assertThat(quote.usdThb().toPlainString()).isEqualTo("33.918378");
        assertThat(quote.quotedAt()).isEqualTo(Instant.ofEpochSecond(1795305752L));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "er-api-error.json, unsupported-code",
            "er-api-missing-thb.json, rates.THB is missing",
            "not-json.html, not valid JSON",
    })
    void rejectsBrokenResponses(String fixture, String expectedMessage) {
        String body = Fixtures.read(fixture);

        assertThatThrownBy(() -> parser.parse(body))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void rejectsImplausibleRate() {
        String body = Fixtures.read("er-api-usd.json").replace("33.918378", "0.0295");

        assertThatThrownBy(() -> parser.parse(body))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("out of plausible range");
    }
}
