package com.goldwatch.goldtracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** World spot gold (XAU/USD) together with the USD/THB rate known at that moment. */
@Entity
@Table(name = "spot_price")
public class SpotPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "xau_usd", nullable = false, precision = 12, scale = 4)
    private BigDecimal xauUsd;

    /** THB per USD; null when no FX rate has been fetched yet. */
    @Column(name = "usd_thb", precision = 10, scale = 4)
    private BigDecimal usdThb;

    /** Quote time, UTC. */
    @Convert(converter = UtcInstantConverter.class)
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(nullable = false, length = 64)
    private String source;

    protected SpotPrice() {
        // for JPA
    }

    public SpotPrice(BigDecimal xauUsd, BigDecimal usdThb, Instant recordedAt, String source) {
        this.xauUsd = Objects.requireNonNull(xauUsd);
        this.usdThb = usdThb;
        this.recordedAt = Objects.requireNonNull(recordedAt).truncatedTo(ChronoUnit.SECONDS);
        this.source = Objects.requireNonNull(source);
    }

    /** True when both values equal the given ones (scale-insensitive, null-safe for the FX rate). */
    public boolean samePrices(BigDecimal otherXauUsd, BigDecimal otherUsdThb) {
        boolean sameFx = usdThb == null ? otherUsdThb == null
                : otherUsdThb != null && usdThb.compareTo(otherUsdThb) == 0;
        return xauUsd.compareTo(otherXauUsd) == 0 && sameFx;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getXauUsd() {
        return xauUsd;
    }

    public BigDecimal getUsdThb() {
        return usdThb;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public String getSource() {
        return source;
    }
}
