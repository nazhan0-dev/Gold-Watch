package com.goldwatch.goldtracker.support;

import com.goldwatch.goldtracker.config.GoldTrackerProperties;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

/** Default {@link GoldTrackerProperties} for plain unit tests (no Spring context). */
public final class TestProperties {

    private TestProperties() {
    }

    public static GoldTrackerProperties defaults() {
        return new GoldTrackerProperties(
                ZoneId.of("Asia/Bangkok"),
                new GoldTrackerProperties.Cors(List.of("http://localhost:3000")),
                new GoldTrackerProperties.Cache(Duration.ofSeconds(30)),
                new GoldTrackerProperties.Scheduler(false, Duration.ofMinutes(5), Duration.ofMinutes(1),
                        Duration.ofSeconds(10)),
                new GoldTrackerProperties.Http(Duration.ofSeconds(5), Duration.ofSeconds(10), "test-agent"),
                new GoldTrackerProperties.Sources(
                        new GoldTrackerProperties.GoldApi("https://api.gold-api.com/price/XAU"),
                        new GoldTrackerProperties.ErApi("https://open.er-api.com/v6/latest/USD",
                                Duration.ofHours(1), Duration.ofHours(48)),
                        new GoldTrackerProperties.GoldTraders("https://www.goldtraders.or.th/")));
    }
}
