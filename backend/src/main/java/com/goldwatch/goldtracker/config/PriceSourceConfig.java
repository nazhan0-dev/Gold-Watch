package com.goldwatch.goldtracker.config;

import com.goldwatch.goldtracker.source.FallbackPriceSource;
import com.goldwatch.goldtracker.source.PriceSource;
import com.goldwatch.goldtracker.source.SpotQuote;
import com.goldwatch.goldtracker.source.goldapi.GoldApiSpotSource;
import com.goldwatch.goldtracker.source.swissquote.SwissquoteSpotSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Chooses which provider serves each quote type. Swap or reorder providers here only. */
@Configuration(proxyBeanMethods = false)
public class PriceSourceConfig {

    /** XAU/USD: gold-api.com first, Swissquote when gold-api.com cannot be reached. */
    @Bean
    @Primary
    PriceSource<SpotQuote> spotPriceSource(GoldApiSpotSource goldApi, SwissquoteSpotSource swissquote) {
        return new FallbackPriceSource<>(goldApi, swissquote);
    }
}
