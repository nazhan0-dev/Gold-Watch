package com.goldwatch.goldtracker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goldwatch.goldtracker.api.dto.HistoryPoint;
import com.goldwatch.goldtracker.api.dto.HistoryResponse;
import com.goldwatch.goldtracker.domain.GoldPrice;
import com.goldwatch.goldtracker.domain.GoldType;
import com.goldwatch.goldtracker.domain.HistoryRange;
import com.goldwatch.goldtracker.domain.PriceSeries;
import com.goldwatch.goldtracker.domain.SpotPrice;
import com.goldwatch.goldtracker.repository.GoldPriceRepository;
import com.goldwatch.goldtracker.repository.SpotPriceRepository;
import com.goldwatch.goldtracker.service.PriceIngestionService;
import com.goldwatch.goldtracker.service.PriceQueryService;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import com.goldwatch.goldtracker.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full stack against real MySQL: SQL bucket aggregation for every range, latest/yesterday comparison,
 * announcement rounds and change detection on ingestion. "Now" is fixed at Sunday 2026-11-22 14:30 Bangkok time.
 */
@MySqlIntegrationTest
@AutoConfigureMockMvc
@Import(PriceApiIntegrationTest.FixedClock.class)
class PriceApiIntegrationTest {

    static final ZoneId BKK = ZoneId.of("Asia/Bangkok");
    static final Instant NOW = bkk("2026-11-22T14:30");

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    PriceQueryService queries;

    @Autowired
    PriceIngestionService ingestion;

    @Autowired
    GoldPriceRepository goldPrices;

    @Autowired
    SpotPriceRepository spotPrices;

    @Autowired
    CacheManager cacheManager;

    static Instant bkk(String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(BKK).toInstant();
    }

    private void bar(String at, String buy, String sell) {
        goldPrices.save(new GoldPrice(GoldType.BAR, new BigDecimal(buy), new BigDecimal(sell), bkk(at), "test"));
    }

    private void ornament(String at, String buy, String sell) {
        goldPrices.save(new GoldPrice(GoldType.ORNAMENT, new BigDecimal(buy), new BigDecimal(sell), bkk(at), "test"));
    }

    private void spot(String utc, String xau, String thb) {
        spotPrices.save(new SpotPrice(new BigDecimal(xau), thb == null ? null : new BigDecimal(thb),
                Instant.parse(utc), "test"));
    }

    @BeforeEach
    void seed() {
        goldPrices.deleteAll();
        spotPrices.deleteAll();
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        bar("2025-06-01T10:00", "39800.00", "40000.00");   // outside every range
        bar("2026-03-02T10:00", "49800.00", "50000.00");   // 1Y only (a Monday)
        bar("2026-11-10T10:00", "55800.00", "56000.00");   // 1M and 1Y
        bar("2026-11-21T09:00", "56800.00", "57000.00");   // yesterday round 1
        bar("2026-11-21T15:00", "56900.00", "57100.00");   // yesterday close
        bar("2026-11-22T09:09", "56950.00", "57150.00");   // today round 1
        bar("2026-11-22T10:30", "57050.00", "57250.00");   // today round 2 (day high)
        bar("2026-11-22T14:05", "57000.00", "57200.00");   // today round 3 (latest)

        ornament("2026-11-21T15:00", "55700.00", "57900.00");
        ornament("2026-11-22T09:09", "55803.96", "57950.00");
        ornament("2026-11-22T10:30", "55901.20", "58050.00");
        ornament("2026-11-22T14:05", "55852.58", "58000.00");

        spot("2026-11-21T10:00:00Z", "3520.0000", "33.8000");   // yesterday (Bangkok) close
        spot("2026-11-22T01:00:00Z", "3535.0000", "33.9000");
        spot("2026-11-22T07:00:00Z", "3539.5000", "33.9184");
    }

    // ------------------------------------------------------------------ history: one test per range

    @Test
    void oneDayIsHourlyOverTheLast24Hours() {
        HistoryResponse res = queries.history(PriceSeries.BAR, HistoryRange.ONE_DAY);

        assertThat(res.interval()).isEqualTo("hour");
        assertThat(res.from()).isEqualTo(bkk("2026-11-21T15:00"));
        assertThat(res.points()).extracting(HistoryPoint::time).containsExactly(
                bkk("2026-11-21T15:00"), bkk("2026-11-22T09:00"), bkk("2026-11-22T10:00"), bkk("2026-11-22T14:00"));
        assertThat(res.points()).extracting(HistoryPoint::close)
                .containsExactly("57100.00", "57150.00", "57250.00", "57200.00");
        assertThat(res.stats().open()).isEqualTo("57100.00");
        assertThat(res.stats().close()).isEqualTo("57200.00");
        assertThat(res.stats().high()).isEqualTo("57250.00");
        assertThat(res.stats().low()).isEqualTo("57100.00");
        assertThat(res.stats().change()).isEqualTo("100.00");
        assertThat(res.stats().changePercent()).isEqualTo("0.18");
    }

    @Test
    void oneWeekIsDailyWithCloseOpenHighLowPerDay() {
        HistoryResponse res = queries.history(PriceSeries.BAR, HistoryRange.ONE_WEEK);

        assertThat(res.interval()).isEqualTo("day");
        assertThat(res.from()).isEqualTo(bkk("2026-11-16T00:00"));
        assertThat(res.points()).hasSize(2);
        HistoryPoint yesterday = res.points().get(0);
        HistoryPoint today = res.points().get(1);
        assertThat(yesterday.time()).isEqualTo(bkk("2026-11-21T00:00"));
        assertThat(yesterday.open()).isEqualTo("57000.00");
        assertThat(yesterday.close()).isEqualTo("57100.00");
        assertThat(today.open()).isEqualTo("57150.00");
        assertThat(today.high()).isEqualTo("57250.00");
        assertThat(today.low()).isEqualTo("57150.00");
        assertThat(today.close()).isEqualTo("57200.00");
        assertThat(res.stats().change()).isEqualTo("200.00");
        assertThat(res.stats().changePercent()).isEqualTo("0.35");
    }

    @Test
    void oneMonthIsDailyOver30Days() {
        HistoryResponse res = queries.history(PriceSeries.BAR, HistoryRange.ONE_MONTH);

        assertThat(res.from()).isEqualTo(bkk("2026-10-24T00:00"));
        assertThat(res.points()).extracting(HistoryPoint::time)
                .containsExactly(bkk("2026-11-10T00:00"), bkk("2026-11-21T00:00"), bkk("2026-11-22T00:00"));
        assertThat(res.stats().low()).isEqualTo("56000.00");
        assertThat(res.stats().change()).isEqualTo("1200.00");
    }

    @Test
    void oneYearIsWeeklyStartingOnMonday() {
        HistoryResponse res = queries.history(PriceSeries.BAR, HistoryRange.ONE_YEAR);

        assertThat(res.interval()).isEqualTo("week");
        assertThat(res.from()).isEqualTo(bkk("2025-11-24T00:00"));
        assertThat(res.points()).extracting(HistoryPoint::time)
                .containsExactly(bkk("2026-03-02T00:00"), bkk("2026-11-09T00:00"), bkk("2026-11-16T00:00"));
        HistoryPoint thisWeek = res.points().get(2);
        assertThat(thisWeek.open()).isEqualTo("57000.00");
        assertThat(thisWeek.close()).isEqualTo("57200.00");
        assertThat(res.stats().open()).isEqualTo("50000.00");
        assertThat(res.stats().low()).isEqualTo("50000.00");
    }

    @Test
    void spotAndOrnamentUseTheirOwnColumns() {
        HistoryResponse spot = queries.history(PriceSeries.SPOT, HistoryRange.ONE_DAY);
        HistoryResponse ornament = queries.history(PriceSeries.ORNAMENT, HistoryRange.ONE_WEEK);

        assertThat(spot.unit()).isEqualTo("USD");
        assertThat(spot.points()).extracting(HistoryPoint::close)
                .containsExactly("3520.0000", "3535.0000", "3539.5000");
        assertThat(ornament.unit()).isEqualTo("THB");
        assertThat(ornament.points()).extracting(HistoryPoint::close).containsExactly("57900.00", "58000.00");
    }

    @Test
    void emptyRangeHasNoPointsAndNoStats() {
        goldPrices.deleteAll();

        HistoryResponse res = queries.history(PriceSeries.BAR, HistoryRange.ONE_DAY);

        assertThat(res.points()).isEmpty();
        assertThat(res.stats()).isNull();
    }

    // ------------------------------------------------------------------ latest + announcements over HTTP

    @Test
    void latestComparesWithYesterdaysClose() throws Exception {
        mvc.perform(get("/api/prices/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bar.buy").value("57000.00"))
                .andExpect(jsonPath("$.bar.sell").value("57200.00"))
                .andExpect(jsonPath("$.bar.previousClose").value("57100.00"))
                .andExpect(jsonPath("$.bar.change").value("100.00"))
                .andExpect(jsonPath("$.bar.changePercent").value("0.18"))
                .andExpect(jsonPath("$.ornament.sell").value("58000.00"))
                .andExpect(jsonPath("$.ornament.change").value("100.00"))
                .andExpect(jsonPath("$.spot.price").value("3539.5000"))
                .andExpect(jsonPath("$.spot.change").value("19.5000"))
                .andExpect(jsonPath("$.spot.changePercent").value("0.55"))
                .andExpect(jsonPath("$.usdThb.price").value("33.9184"))
                .andExpect(jsonPath("$.usdThb.changePercent").value("0.35"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-22T07:05:00Z"));
    }

    @Test
    void announcementsAreNewestFirstWithPerRoundChange() throws Exception {
        mvc.perform(get("/api/prices/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-22"))
                .andExpect(jsonPath("$.rounds", hasSize(3)))
                .andExpect(jsonPath("$.rounds[0].round").value(3))
                .andExpect(jsonPath("$.rounds[0].announcedAt").value("2026-11-22T07:05:00Z"))
                .andExpect(jsonPath("$.rounds[0].change").value("-50.00"))
                .andExpect(jsonPath("$.rounds[0].ornamentSell").value("58000.00"))
                .andExpect(jsonPath("$.rounds[1].change").value("100.00"))
                .andExpect(jsonPath("$.rounds[2].round").value(1))
                .andExpect(jsonPath("$.rounds[2].change").value("50.00"));

        mvc.perform(get("/api/prices/announcements").param("date", "2026-11-21"))
                .andExpect(jsonPath("$.rounds", hasSize(2)))
                .andExpect(jsonPath("$.rounds[1].change").value("1000.00"));
    }

    @Test
    void historyEndpointServesAggregatedJson() throws Exception {
        mvc.perform(get("/api/prices/history").param("type", "bar").param("range", "1W"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points", hasSize(2)))
                .andExpect(jsonPath("$.points[1].close").value("57200.00"))
                .andExpect(jsonPath("$.stats.high").value("57250.00"));
    }

    // ------------------------------------------------------------------ ingestion change detection

    @Test
    void ingestionWritesOnlyWhenAnnouncementChanges() {
        goldPrices.deleteAll();
        ThaiGoldQuote round1 = new ThaiGoldQuote(bkk("2026-11-22T09:09"), 1, new BigDecimal("56950.00"),
                new BigDecimal("57150.00"), new BigDecimal("55803.96"), new BigDecimal("57950.00"));

        assertThat(ingestion.saveThaiGold(round1, "goldtraders.or.th")).isEqualTo(2);
        assertThat(ingestion.saveThaiGold(round1, "goldtraders.or.th")).isZero();
        assertThat(goldPrices.count()).isEqualTo(2);
    }
}
