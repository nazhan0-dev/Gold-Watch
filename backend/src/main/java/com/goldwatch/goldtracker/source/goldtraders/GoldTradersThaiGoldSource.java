package com.goldwatch.goldtracker.source.goldtraders;

import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import com.goldwatch.goldtracker.source.HttpFetcher;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import java.time.Clock;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Thai gold bar / ornament prices scraped from the public home page of the Gold Traders Association.
 *
 * <p>robots.txt (checked 2026-11-22) allows {@code /} for generic user agents and disallows {@code /api/},
 * so only the home page HTML is fetched, at most once per scheduler interval.
 */
@Component
public class GoldTradersThaiGoldSource implements PriceSource<ThaiGoldQuote> {

    public static final String NAME = "goldtraders.or.th";

    /** Tolerated clock skew between the site and this server. */
    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(10);

    private final HttpFetcher http;
    private final String url;
    private final GoldTradersHtmlParser parser;
    private final Clock clock;

    public GoldTradersThaiGoldSource(HttpFetcher http, GoldTrackerProperties properties, Clock clock) {
        this.http = http;
        this.url = properties.sources().goldtraders().url();
        this.parser = new GoldTradersHtmlParser(properties.zone());
        this.clock = clock;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ThaiGoldQuote fetch() {
        ThaiGoldQuote quote = parser.parse(http.get(url, MediaType.TEXT_HTML));
        if (quote.announcedAt().isAfter(clock.instant().plus(MAX_FUTURE_SKEW))) {
            throw new PriceSourceException(NAME + ": announcement time " + quote.announcedAt() + " is in the future");
        }
        return quote;
    }
}
