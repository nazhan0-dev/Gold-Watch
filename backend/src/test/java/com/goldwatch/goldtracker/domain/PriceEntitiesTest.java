package com.goldwatch.goldtracker.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PriceEntitiesTest {

    private static final Instant AT = Instant.parse("2026-11-22T02:09:00Z");

    @Test
    void goldPriceComparesAnnouncementIgnoringDecimalScale() {
        GoldPrice price = new GoldPrice(GoldType.BAR, new BigDecimal("56950.00"), new BigDecimal("57150.00"), AT, "t");

        assertThat(price.sameAnnouncement(AT, new BigDecimal("56950"), new BigDecimal("57150.0"))).isTrue();
        assertThat(price.sameAnnouncement(AT, new BigDecimal("56950"), new BigDecimal("57200"))).isFalse();
        assertThat(price.sameAnnouncement(AT.plusSeconds(60), new BigDecimal("56950"), new BigDecimal("57150"))).isFalse();
    }

    @Test
    void spotPriceComparisonHandlesMissingFxRate() {
        SpotPrice withoutFx = new SpotPrice(new BigDecimal("3539.0"), null, AT, "t");
        SpotPrice withFx = new SpotPrice(new BigDecimal("3539.0"), new BigDecimal("33.9184"), AT, "t");

        assertThat(withoutFx.samePrices(new BigDecimal("3539.0000"), null)).isTrue();
        assertThat(withoutFx.samePrices(new BigDecimal("3539"), new BigDecimal("33.9"))).isFalse();
        assertThat(withFx.samePrices(new BigDecimal("3539"), new BigDecimal("33.918400"))).isTrue();
        assertThat(withFx.samePrices(new BigDecimal("3539"), null)).isFalse();
    }
}
