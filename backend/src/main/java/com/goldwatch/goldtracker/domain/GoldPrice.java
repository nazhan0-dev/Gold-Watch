package com.goldwatch.goldtracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** One Thai gold price (per baht-weight, 96.5%) for one announcement round. */
@Entity
@Table(name = "gold_price")
public class GoldPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GoldType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal buy;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sell;

    /** Announcement time, UTC. */
    @Convert(converter = UtcInstantConverter.class)
    @Column(name = "announced_at", nullable = false)
    private Instant announcedAt;

    @Column(nullable = false, length = 64)
    private String source;

    protected GoldPrice() {
        // for JPA
    }

    public GoldPrice(GoldType type, BigDecimal buy, BigDecimal sell, Instant announcedAt, String source) {
        this.type = Objects.requireNonNull(type);
        this.buy = Objects.requireNonNull(buy);
        this.sell = Objects.requireNonNull(sell);
        this.announcedAt = Objects.requireNonNull(announcedAt).truncatedTo(ChronoUnit.SECONDS);
        this.source = Objects.requireNonNull(source);
    }

    /** True when this row carries the same announcement and prices (scale-insensitive). */
    public boolean sameAnnouncement(Instant otherAnnouncedAt, BigDecimal otherBuy, BigDecimal otherSell) {
        return announcedAt.equals(otherAnnouncedAt)
                && buy.compareTo(otherBuy) == 0
                && sell.compareTo(otherSell) == 0;
    }

    public Long getId() {
        return id;
    }

    public GoldType getType() {
        return type;
    }

    public BigDecimal getBuy() {
        return buy;
    }

    public BigDecimal getSell() {
        return sell;
    }

    public Instant getAnnouncedAt() {
        return announcedAt;
    }

    public String getSource() {
        return source;
    }
}
