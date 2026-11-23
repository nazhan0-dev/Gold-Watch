package com.goldwatch.goldtracker.config;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Typed view of the {@code goldtracker.*} section of application.yml. */
@ConfigurationProperties("goldtracker")
public record GoldTrackerProperties(
        @DefaultValue("Asia/Bangkok") ZoneId zone,
        @DefaultValue Cors cors,
        @DefaultValue Cache cache,
        @DefaultValue Scheduler scheduler,
        @DefaultValue Http http,
        @DefaultValue Sources sources) {

    public record Cors(@DefaultValue("http://localhost:3000") List<String> allowedOrigins) {
    }

    public record Cache(@DefaultValue("PT30S") Duration latestTtl) {
    }

    public record Scheduler(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("PT5M") Duration interval,
            @DefaultValue("PT1M") Duration spotInterval,
            @DefaultValue("PT10S") Duration initialDelay) {
    }

    public record Http(
            @DefaultValue("PT5S") Duration connectTimeout,
            @DefaultValue("PT10S") Duration readTimeout,
            @DefaultValue("GoldPriceTracker/0.1") String userAgent) {
    }

    public record Sources(
            @DefaultValue GoldApi goldApi,
            @DefaultValue ErApi erApi,
            @DefaultValue GoldTraders goldtraders,
            @DefaultValue Swissquote swissquote) {
    }

    public record GoldApi(@DefaultValue("https://api.gold-api.com/price/XAU") String url) {
    }

    /**
     * @param minRefresh do not call the provider more often than this (the rate changes once a day)
     * @param maxAge     a cached rate older than this is no longer attached to new spot rows
     */
    public record ErApi(
            @DefaultValue("https://open.er-api.com/v6/latest/USD") String url,
            @DefaultValue("PT1H") Duration minRefresh,
            @DefaultValue("PT48H") Duration maxAge) {
    }

    /** Fallback XAU/USD feed used when gold-api.com cannot be reached. */
    public record Swissquote(
            @DefaultValue("https://forex-data-feed.swissquote.com/public-quotes/bboquotes/instrument/XAU/USD") String url) {
    }

    public record GoldTraders(@DefaultValue("https://www.goldtraders.or.th/") String url) {
    }
}
