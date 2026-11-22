package com.goldwatch.goldtracker.source.goldapi;

import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.Prices;
import com.goldwatch.goldtracker.source.SpotQuote;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses {@code GET https://api.gold-api.com/price/XAU}, verified on 2026-11-22:
 *
 * <pre>{"currency":"USD","name":"Gold","price":3539.0,"symbol":"XAU","updatedAt":"2026-11-22T06:51:09Z", ...}</pre>
 */
public class GoldApiResponseParser {

    /** Floats become BigDecimal so no precision is lost through double. */
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    public SpotQuote parse(String json) {
        JsonNode root;
        try {
            root = MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new PriceSourceException("gold-api.com: response is not valid JSON", e);
        }
        if (root == null || !root.isObject()) {
            throw new PriceSourceException("gold-api.com: expected a JSON object");
        }
        String symbol = root.path("symbol").asString("");
        String currency = root.path("currency").asString("");
        if (!"XAU".equals(symbol) || !"USD".equals(currency)) {
            throw new PriceSourceException("gold-api.com: expected XAU in USD but got " + symbol + " in " + currency);
        }
        JsonNode priceNode = root.path("price");
        if (!priceNode.isNumber()) {
            throw new PriceSourceException("gold-api.com: price is missing or not numeric");
        }
        BigDecimal price = Prices.requireInRange(priceNode.decimalValue(), "gold-api.com price", "100", "100000");

        String updatedAt = root.path("updatedAt").asString("");
        try {
            return new SpotQuote(price, Instant.parse(updatedAt));
        } catch (DateTimeParseException e) {
            throw new PriceSourceException("gold-api.com: invalid updatedAt '" + updatedAt + "'", e);
        }
    }
}
