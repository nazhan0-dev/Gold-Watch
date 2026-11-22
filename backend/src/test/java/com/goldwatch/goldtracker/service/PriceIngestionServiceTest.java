package com.goldwatch.goldtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goldwatch.goldtracker.domain.GoldPrice;
import com.goldwatch.goldtracker.domain.GoldType;
import com.goldwatch.goldtracker.domain.SpotPrice;
import com.goldwatch.goldtracker.repository.GoldPriceRepository;
import com.goldwatch.goldtracker.repository.SpotPriceRepository;
import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.SpotQuote;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceIngestionServiceTest {

    private static final Instant ROUND_1 = Instant.parse("2026-11-22T02:09:00Z");
    private static final Instant ROUND_2 = Instant.parse("2026-11-22T03:15:00Z");

    @Mock
    GoldPriceRepository goldPrices;

    @Mock
    SpotPriceRepository spotPrices;

    @InjectMocks
    PriceIngestionService service;

    private static ThaiGoldQuote quote(Instant at, String barSell) {
        return new ThaiGoldQuote(at, 1, new BigDecimal("56950.00"), new BigDecimal(barSell),
                new BigDecimal("55803.96"), new BigDecimal("57950.00"));
    }

    private static BigDecimal d(String value) {
        return new BigDecimal(value);
    }

    @Test
    void storesBothTypesWhenTableIsEmpty() {
        when(goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(any())).thenReturn(Optional.empty());

        int inserted = service.saveThaiGold(quote(ROUND_1, "57150.00"), "goldtraders.or.th");

        assertThat(inserted).isEqualTo(2);
        ArgumentCaptor<GoldPrice> saved = ArgumentCaptor.forClass(GoldPrice.class);
        verify(goldPrices, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(GoldPrice::getType).containsExactly(GoldType.BAR, GoldType.ORNAMENT);
        assertThat(saved.getAllValues().getFirst().getAnnouncedAt()).isEqualTo(ROUND_1);
    }

    @Test
    void skipsUnchangedAnnouncement() {
        when(goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(GoldType.BAR)).thenReturn(Optional.of(
                new GoldPrice(GoldType.BAR, d("56950"), d("57150"), ROUND_1, "x")));
        when(goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(GoldType.ORNAMENT)).thenReturn(Optional.of(
                new GoldPrice(GoldType.ORNAMENT, d("55803.96"), d("57950"), ROUND_1, "x")));

        assertThat(service.saveThaiGold(quote(ROUND_1, "57150.00"), "x")).isZero();
        verify(goldPrices, never()).save(any());
    }

    @Test
    void storesNewRoundEvenWhenPriceIsUnchanged() {
        when(goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(GoldType.BAR)).thenReturn(Optional.of(
                new GoldPrice(GoldType.BAR, d("56950"), d("57150"), ROUND_1, "x")));
        when(goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(GoldType.ORNAMENT)).thenReturn(Optional.of(
                new GoldPrice(GoldType.ORNAMENT, d("55803.96"), d("57950"), ROUND_1, "x")));

        assertThat(service.saveThaiGold(quote(ROUND_2, "57150.00"), "x")).isEqualTo(2);
    }

    @Test
    void ignoresAnnouncementOlderThanLatestRow() {
        when(goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(any())).thenReturn(Optional.of(
                new GoldPrice(GoldType.BAR, d("1"), d("2"), ROUND_2, "x")));

        assertThat(service.saveThaiGold(quote(ROUND_1, "57150.00"), "x")).isZero();
        verify(goldPrices, never()).save(any());
    }

    @Test
    void spotIsStoredOnlyWhenPriceOrRateChanges() {
        Instant t = Instant.parse("2026-11-22T07:00:00Z");
        when(spotPrices.findFirstByOrderByRecordedAtDescIdDesc()).thenReturn(Optional.of(
                new SpotPrice(d("3539.0000"), d("33.9184"), t, "x")));

        boolean same = service.saveSpot(new SpotQuote(d("3539.0"), t.plusSeconds(300)),
                new FxQuote(d("33.918378"), t), "x");
        boolean changed = service.saveSpot(new SpotQuote(d("3540.5"), t.plusSeconds(600)),
                new FxQuote(d("33.918378"), t), "x");

        assertThat(same).isFalse();
        assertThat(changed).isTrue();
        ArgumentCaptor<SpotPrice> saved = ArgumentCaptor.forClass(SpotPrice.class);
        verify(spotPrices).save(saved.capture());
        assertThat(saved.getValue().getXauUsd().toPlainString()).isEqualTo("3540.5000");
        assertThat(saved.getValue().getUsdThb().toPlainString()).isEqualTo("33.9184");
    }

    @Test
    void spotWithoutFxRateIsStoredWithNullRate() {
        when(spotPrices.findFirstByOrderByRecordedAtDescIdDesc()).thenReturn(Optional.empty());

        assertThat(service.saveSpot(new SpotQuote(d("3539"), ROUND_1), null, "gold-api.com")).isTrue();

        ArgumentCaptor<SpotPrice> saved = ArgumentCaptor.forClass(SpotPrice.class);
        verify(spotPrices).save(saved.capture());
        assertThat(saved.getValue().getUsdThb()).isNull();
    }
}
