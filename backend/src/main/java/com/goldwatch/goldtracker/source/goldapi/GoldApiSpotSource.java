package com.goldwatch.goldtracker.source.goldapi;

import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import com.goldwatch.goldtracker.source.HttpFetcher;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.SpotQuote;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** World spot gold (XAU/USD) from the free, key-less gold-api.com. */
@Component
public class GoldApiSpotSource implements PriceSource<SpotQuote> {

    public static final String NAME = "gold-api.com";

    private final HttpFetcher http;
    private final String url;
    private final GoldApiResponseParser parser = new GoldApiResponseParser();

    public GoldApiSpotSource(HttpFetcher http, GoldTrackerProperties properties) {
        this.http = http;
        this.url = properties.sources().goldApi().url();
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
