package com.goldwatch.goldtracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Short in-memory cache for /api/prices/latest (default 30 s, {@code goldtracker.cache.latest-ttl}). */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    public static final String LATEST_PRICES = "latestPrices";

    @Bean
    CacheManager cacheManager(GoldTrackerProperties properties) {
        CaffeineCacheManager manager = new CaffeineCacheManager(LATEST_PRICES);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(properties.cache().latestTtl())
                .maximumSize(10));
        return manager;
    }
}
