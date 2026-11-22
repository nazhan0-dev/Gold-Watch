package com.goldwatch.goldtracker.source.goldtraders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import com.goldwatch.goldtracker.support.Fixtures;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GoldTradersHtmlParserTest {

    private final GoldTradersHtmlParser parser = new GoldTradersHtmlParser(ZoneId.of("Asia/Bangkok"));

    @Test
    void parsesRealHomePage() {
        ThaiGoldQuote quote = parser.parse(Fixtures.read("goldtraders-home.html"));

        assertThat(quote.barBuy()).isEqualByComparingTo("56950.00");
        assertThat(quote.barSell()).isEqualByComparingTo("57150.00");
        assertThat(quote.ornamentBuy()).isEqualByComparingTo("55803.96");
        assertThat(quote.ornamentSell()).isEqualByComparingTo("57950.00");
        assertThat(quote.round()).isEqualTo(1);
        // 22/11/2569 09:09 Bangkok time (Buddhist Era) == 2026-11-22 02:09 UTC
        assertThat(quote.announcedAt()).isEqualTo(Instant.parse("2026-11-22T02:09:00Z"));
    }

    @Test
    void worksWithoutTheShareCardWrapper() {
        String html = Fixtures.read("goldtraders-home.html").replace("id=\"gold-prices-share-image\"", "");

        ThaiGoldQuote quote = parser.parse(html);

        assertThat(quote.barSell()).isEqualByComparingTo("57150.00");
        assertThat(quote.ornamentBuy()).isEqualByComparingTo("55803.96");
    }

    @Test
    void acceptsGregorianYearAndLaterRounds() {
        String html = Fixtures.read("goldtraders-home.html")
                .replace("22/11/2569 เวลา 09:09 (ครั้งที่ 1)", "22/11/2026 เวลา 14:35 (ครั้งที่ 12)");

        ThaiGoldQuote quote = parser.parse(html);

        assertThat(quote.round()).isEqualTo(12);
        assertThat(quote.announcedAt()).isEqualTo(Instant.parse("2026-11-22T07:35:00Z"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "goldtraders-empty-values.html, not a number",
            "goldtraders-malformed-number.html, not a number",
            "goldtraders-changed-structure.html, announcement",
            "goldtraders-missing-announcement.html, announcement",
            "goldtraders-loading-shell.html, announcement",
    })
    void rejectsBrokenPages(String fixture, String expectedMessage) {
        String html = Fixtures.read(fixture);

        assertThatThrownBy(() -> parser.parse(html))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void rejectsRenamedPriceLabelsEvenWhenHeaderIsIntact() {
        String html = Fixtures.read("goldtraders-home.html").replace("ทองคำแท่ง", "Gold Bar");

        assertThatThrownBy(() -> parser.parse(html))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void rejectsSellBelowBuy() {
        String html = Fixtures.read("goldtraders-home.html").replace("57,150.00", "56,000.00");

        assertThatThrownBy(() -> parser.parse(html))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("below buy");
    }

    @Test
    void rejectsImplausiblePrices() {
        String html = Fixtures.read("goldtraders-home.html").replace("56,950.00", "5.00");

        assertThatThrownBy(() -> parser.parse(html))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("out of plausible range");
    }

    @Test
    void rejectsImpossibleDate() {
        String html = Fixtures.read("goldtraders-home.html").replace("22/11/2569", "31/02/2569");

        assertThatThrownBy(() -> parser.parse(html))
                .isInstanceOf(PriceSourceException.class)
                .hasMessageContaining("invalid announcement date");
    }
}
