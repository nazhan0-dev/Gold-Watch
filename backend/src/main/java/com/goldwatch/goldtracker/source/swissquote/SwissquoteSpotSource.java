package com.goldwatch.goldtracker.source.swissquote;

import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import com.goldwatch.goldtracker.source.HttpFetcher;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.SpotQuote;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * World spot gold (XAU/USD) from Swissquote's public quote feed — the fallback when gold-api.com
 * cannot be reached. Unofficial endpoint (no key): it may change without notice.
 */
@Component
public class SwissquoteSpotSource implements PriceSource<SpotQuote> {

    public static final String NAME = "swissquote.com";

    private final HttpFetcher http;
    private final String url;
    private final SwissquoteResponseParser parser = new SwissquoteResponseParser();

    public SwissquoteSpotSource(HttpFetcher http, GoldTrackerProperties properties) {
        this.http = http;
        this.url = properties.sources().swissquote().url();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public SpotQuote fetch() {
        return parser.parse(http.get(url, MediaType.APPLICATION_JSON));
    }
}
