package com.goldwatch.goldtracker.source.erapi;

import com.goldwatch.goldtracker.source.FxQuote;
import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.Prices;
import java.math.BigDecimal;
import java.time.Instant;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses {@code GET https://open.er-api.com/v6/latest/USD} (ExchangeRate-API open access), verified on 2026-11-22:
 *
 * <pre>{"result":"success","time_last_update_unix":1795305752,"base_code":"USD","rates":{"THB":33.918378, ...}}</pre>
 */
public class ErApiResponseParser {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    public FxQuote parse(String json) {
        JsonNode root;
        try {
            root = MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new PriceSourceException("open.er-api.com: response is not valid JSON", e);
        }
        if (root == null || !root.isObject()) {
            throw new PriceSourceException("open.er-api.com: expected a JSON object");
        }
        String result = root.path("result").asString("");
        if (!"success".equals(result)) {
            throw new PriceSourceException("open.er-api.com: result=" + result
                    + " error-type=" + root.path("error-type").asString("unknown"));
        }
        if (!"USD".equals(root.path("base_code").asString(""))) {
            throw new PriceSourceException("open.er-api.com: base_code is not USD");
        }
        JsonNode thb = root.path("rates").path("THB");
        if (!thb.isNumber()) {
            throw new PriceSourceException("open.er-api.com: rates.THB is missing or not numeric");
        }
        BigDecimal rate = Prices.requireInRange(thb.decimalValue(), "USD/THB", "10", "100");

        JsonNode updated = root.path("time_last_update_unix");
        if (!updated.canConvertToLong()) {
            throw new PriceSourceException("open.er-api.com: time_last_update_unix is missing");
        }
        return new FxQuote(rate, Instant.ofEpochSecond(updated.longValue()));
    }
}
