package com.goldwatch.goldtracker.source.erapi;

import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.HttpFetcher;
import com.goldwatch.goldtracker.source.PriceSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * USD/THB from ExchangeRate-API's free open-access endpoint (no key, updated once a day).
 * Attribution required by its terms: "Rates By Exchange Rate API" linking to https://www.exchangerate-api.com.
 */
@Component
public class ErApiFxSource implements PriceSource<FxQuote> {

    public static final String NAME = "open.er-api.com";

    private final HttpFetcher http;
    private final String url;
    private final ErApiResponseParser parser = new ErApiResponseParser();

    public ErApiFxSource(HttpFetcher http, GoldTrackerProperties properties) {
        this.http = http;
        this.url = properties.sources().erApi().url();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FxQuote fetch() {
        return parser.parse(http.get(url, MediaType.APPLICATION_JSON));
    }
}
