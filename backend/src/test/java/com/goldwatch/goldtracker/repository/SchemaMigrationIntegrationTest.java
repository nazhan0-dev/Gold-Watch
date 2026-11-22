package com.goldwatch.goldtracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.goldwatch.goldtracker.domain.GoldPrice;
import com.goldwatch.goldtracker.domain.GoldType;
import com.goldwatch.goldtracker.domain.SpotPrice;
import com.goldwatch.goldtracker.support.MySqlIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@MySqlIntegrationTest
class SchemaMigrationIntegrationTest {

    @Autowired
    GoldPriceRepository goldPrices;

    @Autowired
    SpotPriceRepository spotPrices;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void cleanUp() {
        goldPrices.deleteAll();
        spotPrices.deleteAll();
    }

    @Test
    void flywayCreatesBothTablesWithIndexes() {
        Integer indexes = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND index_name IN ('idx_gold_price_type_announced_at', 'idx_spot_price_recorded_at')
                """, Integer.class);
        assertThat(indexes).isEqualTo(3); // composite index has two columns
    }

    @Test
    void decimalsAndUtcInstantsRoundTripExactly() {
        Instant at = Instant.parse("2026-11-22T02:09:00Z");
        goldPrices.save(new GoldPrice(GoldType.BAR, new BigDecimal("56950.00"), new BigDecimal("57150.00"), at, "test"));
        spotPrices.save(new SpotPrice(new BigDecimal("3539.1234"), null, at, "test"));

        GoldPrice gold = goldPrices.findFirstByTypeOrderByAnnouncedAtDescIdDesc(GoldType.BAR).orElseThrow();
        assertThat(gold.getSell()).isEqualByComparingTo("57150.00");
        assertThat(gold.getAnnouncedAt()).isEqualTo(at);
        // Stored value in the column itself must be UTC, not the JVM's local time.
        String raw = jdbc.queryForObject("SELECT DATE_FORMAT(announced_at, '%H:%i') FROM gold_price", String.class);
        assertThat(raw).isEqualTo("02:09");

        SpotPrice spot = spotPrices.findFirstByOrderByRecordedAtDescIdDesc().orElseThrow();
        assertThat(spot.getXauUsd()).isEqualByComparingTo("3539.1234");
        assertThat(spot.getUsdThb()).isNull();
    }
}
