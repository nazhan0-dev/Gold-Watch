package com.goldwatch.goldtracker.repository;

import com.goldwatch.goldtracker.domain.HistoryRange.Granularity;
import com.goldwatch.goldtracker.domain.PriceSeries;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Aggregates prices into time buckets inside MySQL (GROUP BY + DATE_FORMAT + window functions), so only one row
 * per bucket ever leaves the database.
 */
@Repository
public class PriceHistoryRepository {

    /** One aggregated bucket. {@code bucketStart} is the bucket's start instant. */
    public record Bucket(Instant bucketStart, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
            int samples) {
    }

    private static final DateTimeFormatter BUCKET_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NamedParameterJdbcTemplate jdbc;

    public PriceHistoryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns buckets between {@code from} (inclusive) and {@code to} (inclusive), oldest first.
     * Close = last price in the bucket, open = first price, high/low = extremes.
     */
    public List<Bucket> aggregate(PriceSeries series, Granularity granularity, Instant from, Instant to, ZoneId zone) {
        // Table/column names and bucket expressions come from enums only, never from user input.
        String table = series == PriceSeries.SPOT ? "spot_price" : "gold_price";
        String timeColumn = series == PriceSeries.SPOT ? "recorded_at" : "announced_at";
        String priceColumn = series == PriceSeries.SPOT ? "xau_usd" : "sell";
        String typeFilter = series == PriceSeries.SPOT ? "" : "type = :type AND ";

        String local = "CONVERT_TZ(" + timeColumn + ", '+00:00', :offset)";
        String bucket = switch (granularity) {
            case HOUR -> "DATE_FORMAT(" + local + ", '%Y-%m-%d %H:00:00')";
            case DAY -> "DATE_FORMAT(" + local + ", '%Y-%m-%d 00:00:00')";
            case WEEK -> "DATE_FORMAT(DATE_SUB(DATE(" + local + "), INTERVAL WEEKDAY(" + local + ") DAY), '%Y-%m-%d 00:00:00')";
        };

        String sql = """
                SELECT bucket,
                       MAX(CASE WHEN rn_first = 1 THEN price END) AS open_price,
                       MAX(price)                                AS high_price,
                       MIN(price)                                AS low_price,
                       MAX(CASE WHEN rn_last = 1 THEN price END)  AS close_price,
                       COUNT(*)                                  AS samples
                FROM (
                    SELECT %1$s AS bucket,
                           %2$s AS price,
                           ROW_NUMBER() OVER (PARTITION BY %1$s ORDER BY %3$s ASC, id ASC)   AS rn_first,
                           ROW_NUMBER() OVER (PARTITION BY %1$s ORDER BY %3$s DESC, id DESC) AS rn_last
                    FROM %4$s
                    WHERE %5$s%3$s >= :from AND %3$s <= :to
                ) ranked
                GROUP BY bucket
                ORDER BY bucket
                """.formatted(bucket, priceColumn, timeColumn, table, typeFilter);

        // Thailand has no DST, but derive the offset from the zone anyway instead of hard-coding +07:00.
        ZoneOffset offset = zone.getRules().getOffset(to);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("offset", offset.getId().equals("Z") ? "+00:00" : offset.getId())
                // Columns hold UTC wall-clock values; pass UTC LocalDateTime so the driver does no conversion.
                .addValue("from", LocalDateTime.ofInstant(from, ZoneOffset.UTC))
                .addValue("to", LocalDateTime.ofInstant(to, ZoneOffset.UTC));
        if (series.goldType() != null) {
            params.addValue("type", series.goldType().name());
        }

        return jdbc.query(sql, params, (rs, rowNum) -> new Bucket(
                LocalDateTime.parse(rs.getString("bucket"), BUCKET_FORMAT).atZone(zone).toInstant(),
                rs.getBigDecimal("open_price"),
                rs.getBigDecimal("high_price"),
                rs.getBigDecimal("low_price"),
                rs.getBigDecimal("close_price"),
                rs.getInt("samples")));
    }
}
