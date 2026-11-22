package com.goldwatch.goldtracker.service;

import com.goldwatch.goldtracker.domain.GoldPrice;
import com.goldwatch.goldtracker.domain.GoldType;
import com.goldwatch.goldtracker.domain.SpotPrice;
import com.goldwatch.goldtracker.repository.GoldPriceRepository;
import com.goldwatch.goldtracker.repository.SpotPriceRepository;
import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.SpotQuote;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persists validated quotes, writing a row only when something actually changed since the latest row. */
@Service
public class PriceIngestionService {

    private static final Logger log = LoggerFactory.getLogger(PriceIngestionService.class);

    private final GoldPriceRepository goldPrices;
    private final SpotPriceRepository spotPrices;

    public PriceIngestionService(GoldPriceRepository goldPrices, SpotPriceRepository spotPrices) {
        this.goldPrices = goldPrices;
        this.spotPrices = spotPrices;
    }

    /**
     * Stores the bar and ornament prices of one announcement round.
     *
     * <p>A row is written when the announcement time or the prices differ from the latest stored row of that type.
     * Keying on the announcement time (not only on the price) keeps every official round, including a first round
     * of the day that repeats yesterday's closing price. Older announcements than the latest row are ignored.
     *
     * @return number of rows inserted (0-2)
     */
    @Transactional
    public int saveThaiGold(ThaiGoldQuote quote, String source) {
        int inserted = 0;
        if (saveIfChanged(GoldType.BAR, quote.barBuy(), quote.barSell(), quote.announcedAt(), source)) {
            inserted++;
        }
        if (saveIfChanged(GoldType.ORNAMENT, quote.ornamentBuy(), quote.ornamentSell(), quote.announcedAt(), source)) {
            inserted++;
        }
        return inserted;
    }

    private boolean saveIfChanged(GoldType type, BigDecimal buy, BigDecimal sell, Instant announcedAt, String source) {
        Optional<GoldPrice> latest = goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(type);
        if (latest.isPresent()) {
            GoldPrice last = latest.get();
            if (last.sameAnnouncement(announcedAt, buy, sell)) {
                return false;
            }
            if (announcedAt.isBefore(last.getAnnouncedAt())) {
                log.warn("Ignoring {} announcement at {} older than stored {}", type, announcedAt, last.getAnnouncedAt());
                return false;
            }
        }
        goldPrices.save(new GoldPrice(type, buy, sell, announcedAt, source));
        log.info("Stored {} buy={} sell={} announcedAt={}", type, buy, sell, announcedAt);
        return true;
    }

    /**
     * Stores a spot quote together with the USD/THB rate currently known (may be null).
     *
     * @return true when a row was inserted
     */
    @Transactional
    public boolean saveSpot(SpotQuote spot, FxQuote fx, String source) {
        BigDecimal xau = spot.xauUsd().setScale(4, RoundingMode.HALF_UP);
        BigDecimal usdThb = fx == null ? null : fx.usdThb().setScale(4, RoundingMode.HALF_UP);

        Optional<SpotPrice> latest = spotPrices.findFirstByOrderByRecordedAtDescIdDesc();
        if (latest.isPresent()) {
            SpotPrice last = latest.get();
            if (last.samePrices(xau, usdThb)) {
                return false;
            }
            if (spot.quotedAt().isBefore(last.getRecordedAt())) {
                log.warn("Ignoring spot quote at {} older than stored {}", spot.quotedAt(), last.getRecordedAt());
                return false;
            }
        }
        spotPrices.save(new SpotPrice(xau, usdThb, spot.quotedAt(), source));
        log.info("Stored spot xauUsd={} usdThb={} recordedAt={}", xau, usdThb, spot.quotedAt());
        return true;
    }

    /** Latest stored FX rate, used to warm the in-memory cache after a restart. */
    @Transactional(readOnly = true)
    public Optional<FxQuote> latestStoredFx() {
        return spotPrices.findFirstByUsdThbIsNotNullOrderByRecordedAtDescIdDesc()
                .map(row -> new FxQuote(row.getUsdThb(), row.getRecordedAt()));
    }
}
