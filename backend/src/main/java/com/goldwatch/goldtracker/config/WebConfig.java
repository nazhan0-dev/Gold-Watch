package com.goldwatch.goldtracker.config;

import com.goldwatch.goldtracker.domain.HistoryRange;
import com.goldwatch.goldtracker.domain.PriceSeries;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GoldTrackerProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final GoldTrackerProperties properties;

    public WebConfig(GoldTrackerProperties properties) {
        this.properties = properties;
    }

    /** Read-only API: only GET from the configured origins (CORS_ALLOWED_ORIGINS). */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(properties.cors().allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET")
                .maxAge(3600);
    }

    /** Lets query parameters use the public codes (bar, spot, 1D, 1W...) instead of enum names. */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, PriceSeries.class, PriceSeries::fromCode);
        registry.addConverter(String.class, HistoryRange.class, HistoryRange::fromCode);
    }
}
