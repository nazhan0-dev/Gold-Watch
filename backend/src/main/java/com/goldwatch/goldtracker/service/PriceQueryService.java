package com.goldwatch.goldtracker.service;

import static com.goldwatch.goldtracker.service.PriceMath.change;
import static com.goldwatch.goldtracker.service.PriceMath.changePercent;
import static com.goldwatch.goldtracker.service.PriceMath.str;

import com.goldwatch.goldtracker.api.dto.AnnouncementRound;
import com.goldwatch.goldtracker.api.dto.AnnouncementsResponse;
import com.goldwatch.goldtracker.api.dto.HistoryPoint;
import com.goldwatch.goldtracker.api.dto.HistoryResponse;
import com.goldwatch.goldtracker.api.dto.HistoryStats;
import com.goldwatch.goldtracker.api.dto.LatestPricesResponse;
import com.goldwatch.goldtracker.api.dto.QuoteLatest;
import com.goldwatch.goldtracker.api.dto.ThaiGoldLatest;
import com.goldwatch.goldtracker.config.CacheConfig;
import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import com.goldwatch.goldtracker.domain.GoldPrice;
import com.goldwatch.goldtracker.domain.GoldType;
import com.goldwatch.goldtracker.domain.HistoryRange;
import com.goldwatch.goldtracker.domain.PriceSeries;
import com.goldwatch.goldtracker.domain.SpotPrice;
import com.goldwatch.goldtracker.repository.GoldPriceRepository;
import com.goldwatch.goldtracker.repository.PriceHistoryRepository;
import com.goldwatch.goldtracker.repository.PriceHistoryRepository.Bucket;
import com.goldwatch.goldtracker.repository.SpotPriceRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only queries behind the REST API. */
@Service
@Transactional(readOnly = true)
public class PriceQueryService {

    private final GoldPriceRepository goldPrices;
    private final SpotPriceRepository spotPrices;
    private final PriceHistoryRepository history;
    private final Clock clock;
    private final ZoneId zone;

    public PriceQueryService(GoldPriceRepository goldPrices, SpotPriceRepository spotPrices,
            PriceHistoryRepository history, Clock clock, GoldTrackerProperties properties) {
        this.goldPrices = goldPrices;
        this.spotPrices = spotPrices;
        this.history = history;
        this.clock = clock;
        this.zone = properties.zone();
    }

    /** Thai calendar date "today". */
    public LocalDate today() {
        return LocalDate.now(clock.withZone(zone));
    }

    private Instant startOfToday() {
        return today().atStartOfDay(zone).toInstant();
    }

    // ---------------------------------------------------------------- latest

    @Cacheable(CacheConfig.LATEST_PRICES)
    public LatestPricesResponse latest() {
        Instant todayStart = startOfToday();
        ThaiGoldLatest bar = thaiGoldLatest(GoldType.BAR, todayStart);
        ThaiGoldLatest ornament = thaiGoldLatest(GoldType.ORNAMENT, todayStart);

        QuoteLatest spot = spotPrices.findFirstByOrderByRecordedAtDescIdDesc()
                .map(latest -> quote(latest.getXauUsd(), "USD",
                        spotPrices.findFirstByRecordedAtBeforeOrderByRecordedAtDescIdDesc(todayStart)
                                .map(SpotPrice::getXauUsd).orElse(null),
                        latest.getRecordedAt()))
                .orElse(null);

        QuoteLatest usdThb = spotPrices.findFirstByUsdThbIsNotNullOrderByRecordedAtDescIdDesc()
                .map(latest -> quote(latest.getUsdThb(), "THB",
                        spotPrices.findFirstByUsdThbIsNotNullAndRecordedAtBeforeOrderByRecordedAtDescIdDesc(todayStart)
                                .map(SpotPrice::getUsdThb).orElse(null),
                        latest.getRecordedAt()))
                .orElse(null);

        Instant updatedAt = Stream.of(
                        bar == null ? null : bar.updatedAt(),
                        ornament == null ? null : ornament.updatedAt(),
                        spot == null ? null : spot.updatedAt(),
                        usdThb == null ? null : usdThb.updatedAt())
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return new LatestPricesResponse(bar, ornament, spot, usdThb, updatedAt);
    }

    private ThaiGoldLatest thaiGoldLatest(GoldType type, Instant todayStart) {
        Optional<GoldPrice> latest = goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(type);
        if (latest.isEmpty()) {
            return null;
        }
        GoldPrice now = latest.get();
        BigDecimal previousClose = goldPrices
                .findFirstByTypeAndAnnouncedAtBeforeOrderByAnnouncedAtDescIdDesc(type, todayStart)
                .map(GoldPrice::getSell)
                .orElse(null);
        return new ThaiGoldLatest(str(now.getBuy()), str(now.getSell()), str(previousClose),
                str(change(now.getSell(), previousClose)), str(changePercent(now.getSell(), previousClose)),
                now.getAnnouncedAt());
    }

    private static QuoteLatest quote(BigDecimal price, String unit, BigDecimal previousClose, Instant updatedAt) {
        return new QuoteLatest(str(price), unit, str(previousClose), str(change(price, previousClose)),
                str(changePercent(price, previousClose)), updatedAt);
    }

    // ---------------------------------------------------------------- history

    public HistoryResponse history(PriceSeries series, HistoryRange range) {
        Instant to = clock.instant();
        Instant from = range.windowStart(to, zone);
        List<Bucket> buckets = history.aggregate(series, range.granularity(), from, to, zone);

        List<HistoryPoint> points = buckets.stream()
                .map(b -> new HistoryPoint(b.bucketStart(), str(b.open()), str(b.high()), str(b.low()), str(b.close())))
                .toList();

        HistoryStats stats = null;
        if (!buckets.isEmpty()) {
            BigDecimal open = buckets.getFirst().open();
            BigDecimal close = buckets.getLast().close();
            BigDecimal high = buckets.stream().map(Bucket::high).max(Comparator.naturalOrder()).orElseThrow();
            BigDecimal low = buckets.stream().map(Bucket::low).min(Comparator.naturalOrder()).orElseThrow();
            stats = new HistoryStats(str(open), str(close), str(high), str(low),
                    str(change(close, open)), str(changePercent(close, open)));
        }
        return new HistoryResponse(series.code(), range.code(),
                range.granularity().name().toLowerCase(), series.unit(), from, to, points, stats);
    }

    // ---------------------------------------------------------------- announcements

    public AnnouncementsResponse announcements(LocalDate date) {
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        // A day has at most a few dozen rounds, so pairing bar/ornament rows in memory is cheap.
        Map<Instant, GoldPrice[]> byTime = new LinkedHashMap<>();
        for (GoldPrice row : goldPrices
                .findByAnnouncedAtGreaterThanEqualAndAnnouncedAtLessThanOrderByAnnouncedAtAscIdAsc(dayStart, dayEnd)) {
            GoldPrice[] pair = byTime.computeIfAbsent(row.getAnnouncedAt(), t -> new GoldPrice[2]);
            pair[row.getType() == GoldType.BAR ? 0 : 1] = row; // later rows (corrections) win
        }

        BigDecimal previousBarSell = goldPrices
                .findFirstByTypeAndAnnouncedAtBeforeOrderByAnnouncedAtDescIdDesc(GoldType.BAR, dayStart)
                .map(GoldPrice::getSell)
                .orElse(null);

        List<AnnouncementRound> rounds = new ArrayList<>();
        int round = 0;
        for (Map.Entry<Instant, GoldPrice[]> entry : byTime.entrySet()) {
            GoldPrice bar = entry.getValue()[0];
            GoldPrice ornament = entry.getValue()[1];
            BigDecimal barSell = bar == null ? null : bar.getSell();
            rounds.add(new AnnouncementRound(++round, entry.getKey(),
                    bar == null ? null : str(bar.getBuy()), str(barSell),
                    ornament == null ? null : str(ornament.getBuy()), ornament == null ? null : str(ornament.getSell()),
                    str(change(barSell, previousBarSell))));
            if (barSell != null) {
                previousBarSell = barSell;
            }
        }
        return new AnnouncementsResponse(date, rounds.reversed());
    }
}
