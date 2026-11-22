package com.goldwatch.goldtracker.source.goldtraders;

import com.goldwatch.goldtracker.source.PriceSourceException;
import com.goldwatch.goldtracker.source.Prices;
import com.goldwatch.goldtracker.source.ThaiGoldQuote;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Parses the server-rendered home page of the Gold Traders Association (https://www.goldtraders.or.th/).
 *
 * <p>Structure verified on 2026-11-22. The page renders, in document order, a header such as
 * {@code ประจำวันที่ 22/11/2569 เวลา 09:09 (ครั้งที่ 1)} followed by two blocks:
 *
 * <pre>
 * ทองคำแท่ง   96.5%  รับซื้อ 56,950.00  ขายออก 57,150.00
 * ทองรูปพรรณ  96.5%  ฐานภาษี 55,803.96  ขายออก 57,950.00
 * </pre>
 *
 * The site labels the ornament buy-back figure "ฐานภาษี" (tax base); it is the number Thai media report as
 * the ornament buy price, so it is stored as {@code buy}. The parser relies on these visible Thai labels rather
 * than CSS class names, and fails loudly (never guesses) when they are missing.
 */
public class GoldTradersHtmlParser {

    static final String BAR_LABEL = "ทองคำแท่ง";
    static final String ORNAMENT_LABEL = "ทองรูปพรรณ";
    private static final String BUY_LABEL = "รับซื้อ";
    private static final String TAX_BASE_LABEL = "ฐานภาษี";
    private static final String SELL_LABEL = "ขายออก";
    private static final Set<String> TYPE_LABELS = Set.of(BAR_LABEL, ORNAMENT_LABEL);

    /** e.g. "ประจำวันที่ 22/11/2569 เวลา 09:09 (ครั้งที่ 1)"; the year is in the Buddhist Era. */
    private static final Pattern ANNOUNCEMENT = Pattern.compile(
            "ประจำวันที่\\s*(\\d{1,2})/(\\d{1,2})/(\\d{4})\\s*เวลา\\s*(\\d{1,2})[:.](\\d{2})\\s*(?:น\\.)?\\s*\\(\\s*ครั้งที่\\s*(\\d+)\\s*\\)");

    private static final String MIN_PRICE = "1000";
    private static final String MAX_PRICE = "1000000";

    private final ZoneId zone;

    public GoldTradersHtmlParser(ZoneId zone) {
        this.zone = zone;
    }

    public ThaiGoldQuote parse(String html) {
        Document doc = Jsoup.parse(html);
        // The share-card block holds the announcement header and both price rows together.
        Element scope = doc.getElementById("gold-prices-share-image");
        if (scope == null) {
            scope = doc.body();
        }

        Announcement announcement = findAnnouncement(scope.text());
        if (announcement == null) {
            announcement = findAnnouncement(doc.text());
        }
        if (announcement == null) {
            throw new PriceSourceException("goldtraders.or.th: announcement date/round not found (page structure changed?)");
        }

        List<String> tokens = textTokens(scope);
        BigDecimal barBuy = valueAfter(tokens, BAR_LABEL, Set.of(BUY_LABEL));
        BigDecimal barSell = valueAfter(tokens, BAR_LABEL, Set.of(SELL_LABEL));
        BigDecimal ornamentBuy = valueAfter(tokens, ORNAMENT_LABEL, Set.of(BUY_LABEL, TAX_BASE_LABEL));
        BigDecimal ornamentSell = valueAfter(tokens, ORNAMENT_LABEL, Set.of(SELL_LABEL));

        requireSellAboveBuy(BAR_LABEL, barBuy, barSell);
        requireSellAboveBuy(ORNAMENT_LABEL, ornamentBuy, ornamentSell);

        return new ThaiGoldQuote(announcement.at().atZone(zone).toInstant(), announcement.round(),
                barBuy, barSell, ornamentBuy, ornamentSell);
    }

    private record Announcement(LocalDateTime at, int round) {
    }

    private Announcement findAnnouncement(String text) {
        Matcher m = ANNOUNCEMENT.matcher(text);
        if (!m.find()) {
            return null;
        }
        int year = Integer.parseInt(m.group(3));
        if (year > 2400) {
            year -= 543; // Buddhist Era -> Gregorian
        }
        try {
            LocalDateTime at = LocalDateTime.of(year, Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)));
            int round = Integer.parseInt(m.group(6));
            if (round < 1) {
                throw new PriceSourceException("goldtraders.or.th: invalid round " + round);
            }
            return new Announcement(at, round);
        } catch (DateTimeException e) {
            throw new PriceSourceException("goldtraders.or.th: invalid announcement date '" + m.group() + "'", e);
        }
    }

    /** Own text of every element in document order; whitespace-only text is skipped. */
    private static List<String> textTokens(Element scope) {
        List<String> tokens = new ArrayList<>();
        for (Element el : scope.getAllElements()) {
            String own = el.ownText().strip();
            if (!own.isEmpty()) {
                tokens.add(own);
            }
        }
        return tokens;
    }

    /** Finds the block that starts at {@code typeLabel} and returns the number right after one of {@code fieldLabels}. */
    private static BigDecimal valueAfter(List<String> tokens, String typeLabel, Set<String> fieldLabels) {
        int start = tokens.indexOf(typeLabel);
        if (start < 0) {
            throw new PriceSourceException("goldtraders.or.th: label '" + typeLabel + "' not found (page structure changed?)");
        }
        String field = typeLabel + "/" + String.join("|", fieldLabels);
        for (int i = start + 1; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (TYPE_LABELS.contains(token)) {
                break; // reached the next block
            }
            if (fieldLabels.contains(token)) {
                String value = i + 1 < tokens.size() ? tokens.get(i + 1) : "";
                BigDecimal price = Prices.parseGrouped(value, "goldtraders.or.th " + field);
                return Prices.requireInRange(price, "goldtraders.or.th " + field, MIN_PRICE, MAX_PRICE);
            }
        }
        throw new PriceSourceException("goldtraders.or.th: field " + field + " not found (page structure changed?)");
    }

    private static void requireSellAboveBuy(String type, BigDecimal buy, BigDecimal sell) {
        if (sell.compareTo(buy) < 0) {
            throw new PriceSourceException("goldtraders.or.th: " + type + " sell " + sell + " is below buy " + buy);
        }
    }
}
