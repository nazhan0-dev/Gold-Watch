package com.goldwatch.goldtracker.source.swissquote;

import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.Prices;
import com.goldwatch.goldtracker.source.SpotQuote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses Swissquote's public bid/ask feed for XAU/USD, verified on 2026-11-23:
 *
 * <pre>[{"topo":{"platform":"SwissquoteCapitalMarkets",...},
 *   "spreadProfilePrices":[{"spreadProfile":"premium","bid":3621.421,"ask":3622.079}, ...],
 *   "ts":1795434341738}, ...]</pre>
 *
 * The stored price is the mid of bid and ask of the first spread profile, preferring the
 * {@code SwissquoteCapitalMarkets} platform when it is present.
 */
public class SwissquoteResponseParser {

    private static final String PREFERRED_PLATFORM = "SwissquoteCapitalMarkets";

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    public SpotQuote parse(String json) {
        JsonNode root;
        try {
            root = MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new PriceSourceException("swissquote: response is not valid JSON", e);
        }
        if (root == null || !root.isArray() || root.isEmpty()) {
            throw new PriceSourceException("swissquote: expected a non-empty JSON array");
        }

        JsonNode entry = root.get(0);
        for (JsonNode candidate : root) {
            if (PREFERRED_PLATFORM.equals(candidate.path("topo").path("platform").asString(""))) {
                entry = candidate;
                break;
            }
        }

        JsonNode prices = entry.path("spreadProfilePrices");
        if (!prices.isArray() || prices.isEmpty()) {
            throw new PriceSourceException("swissquote: spreadProfilePrices is missing");
        }
        JsonNode bidNode = prices.get(0).path("bid");
        JsonNode askNode = prices.get(0).path("ask");
        if (!bidNode.isNumber() || !askNode.isNumber()) {
            throw new PriceSourceException("swissquote: bid/ask is missing or not numeric");
        }
        BigDecimal bid = Prices.requireInRange(bidNode.decimalValue(), "swissquote bid", "100", "100000");
        BigDecimal ask = Prices.requireInRange(askNode.decimalValue(), "swissquote ask", "100", "100000");
        if (ask.compareTo(bid) < 0) {
            throw new PriceSourceException("swissquote: ask " + ask + " is below bid " + bid);
        }
        BigDecimal mid = bid.add(ask).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);

        JsonNode ts = entry.path("ts");
        if (!ts.canConvertToLong()) {
            throw new PriceSourceException("swissquote: ts is missing");
        }
        return new SpotQuote(mid, Instant.ofEpochMilli(ts.longValue()));
    }
}
