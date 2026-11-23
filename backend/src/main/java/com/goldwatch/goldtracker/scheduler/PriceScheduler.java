package com.goldwatch.goldtracker.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Triggers price collection on fixed delays: Thai gold every {@code goldtracker.scheduler.interval} (default
 * 5 minutes, to stay polite to the association's site) and world spot gold every
 * {@code goldtracker.scheduler.spot-interval} (default 1 minute). The two jobs are separate so a slow or failing
 * source never delays the other one.
 * Disable with {@code goldtracker.scheduler.enabled=false} (used by tests).
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(prefix = "goldtracker.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PriceScheduler {

    private final PriceCollector collector;

    public PriceScheduler(PriceCollector collector) {
        this.collector = collector;
    }

    @Scheduled(fixedDelayString = "${goldtracker.scheduler.interval:PT5M}",
            initialDelayString = "${goldtracker.scheduler.initial-delay:PT10S}")
    void collectThaiGold() {
        collector.collectThaiGold();
    }

    @Scheduled(fixedDelayString = "${goldtracker.scheduler.spot-interval:PT1M}",
            initialDelayString = "${goldtracker.scheduler.initial-delay:PT10S}")
    void collectSpot() {
        collector.collectSpot();
    }
}
