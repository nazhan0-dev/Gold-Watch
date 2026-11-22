package com.goldwatch.goldtracker.scheduler;

import com.goldwatch.goldtracker.service.FxRateCache;
import com.goldwatch.goldtracker.service.PriceIngestionService;
import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.SpotQuote;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Pulls each source and hands valid quotes to {@link PriceIngestionService}. Every source is isolated: a failure is
 * logged, nothing is written for that source, and the other sources keep working.
 */
@Component
public class PriceCollector {

    private static final Logger log = LoggerFactory.getLogger(PriceCollector.class);

    private final PriceSource<ThaiGoldQuote> thaiGoldSource;
    private final PriceSource<SpotQuote> spotSource;
    private final FxRateCache fxRates;
    private final PriceIngestionService ingestion;

    public PriceCollector(PriceSource<ThaiGoldQuote> thaiGoldSource, PriceSource<SpotQuote> spotSource,
            FxRateCache fxRates, PriceIngestionService ingestion) {
        this.thaiGoldSource = thaiGoldSource;
        this.spotSource = spotSource;
        this.fxRates = fxRates;
        this.ingestion = ingestion;
    }

    /** @return true when the source answered with valid data (whether or not it changed) */
    public boolean collectThaiGold() {
        try {
            ThaiGoldQuote quote = thaiGoldSource.fetch();
            ingestion.saveThaiGold(quote, thaiGoldSource.name());
            return true;
        } catch (PriceSourceException e) {
            log.error("Price source {} failed: {}", thaiGoldSource.name(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected error while collecting from {}", thaiGoldSource.name(), e);
        }
        return false;
    }

    /** @return true when the spot source answered with valid data (whether or not it changed) */
    public boolean collectSpot() {
        try {
            SpotQuote spot = spotSource.fetch();
            FxQuote fx = fxRates.currentRate();
            String source = fx == null ? spotSource.name() : spotSource.name() + "," + fxRates.sourceName();
            ingestion.saveSpot(spot, fx, source);
            return true;
        } catch (PriceSourceException e) {
            log.error("Price source {} failed: {}", spotSource.name(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected error while collecting from {}", spotSource.name(), e);
        }
        return false;
    }
}
