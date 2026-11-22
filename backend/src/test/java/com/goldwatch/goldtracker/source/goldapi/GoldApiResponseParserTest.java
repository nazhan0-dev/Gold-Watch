package com.goldwatch.goldtracker.source.goldapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.SpotQuote;
import com.goldwatch.goldtracker.support.Fixtures;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GoldApiResponseParserTest {

    private final GoldApiResponseParser parser = new GoldApiResponseParser();

    @Test
    void parsesRealResponse() {
        SpotQuote quote = parser.parse(Fixtures.read("gold-api-xau.json"));

        assertThat(quote.xauUsd()).isEqualByComparingTo("3539.0");
        assertThat(quote.quotedAt()).isEqualTo(Instant.parse("2026-11-22T07:03:09Z"));
    }

    @Test
    void keepsFullDecimalPrecision() {
        SpotQuote quote = parser.parse("""
                {"currency":"USD","price":3539.123456789,"symbol":"XAU","updatedAt":"2026-11-22T07:03:09Z"}
                """);

        assertThat(quote.xauUsd().toPlainString()).isEqualTo("3539.123456789");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "gold-api-missing-price.json, price is missing",
            "gold-api-non-numeric-price.json, price is missing or not numeric",
            "gold-api-wrong-symbol.json, expected XAU",
            "not-json.html, not valid JSON",
    })
    void rejectsBrokenResponses(String fixture, String expectedMessage) {
        String body = Fixtures.read(fixture);

        assertThatThrownBy(() -> parser.parse(body))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void rejectsMissingTimestampAndImplausiblePrice() {
        assertThatThrownBy(() -> parser.parse("""
                {"currency":"USD","price":3539.0,"symbol":"XAU"}"""))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("updatedAt");
        assertThatThrownBy(() -> parser.parse("""
                {"currency":"USD","price":0,"symbol":"XAU","updatedAt":"2026-11-22T07:03:09Z"}"""))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("out of plausible range");
        assertThatThrownBy(() -> parser.parse("[]"))
                .isInstanceOf(PriceSourceException.class);
    }
}
