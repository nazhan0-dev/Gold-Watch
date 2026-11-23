package com.goldwatch.goldtracker.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tries each source in order and returns the first valid quote. {@link #name()} reports the source that
 * answered last, so the stored {@code source} column says where the price really came from.
 *
 * @param <T> quote type
 */
public class FallbackPriceSource<T> implements PriceSource<T> {

    private static final Logger log = LoggerFactory.getLogger(FallbackPriceSource.class);

    private final List<PriceSource<T>> sources;
    private volatile String lastUsed;

    @SafeVarargs
    public FallbackPriceSource(PriceSource<T>... sources) {
        if (sources.length == 0) {
            throw new IllegalArgumentException("At least one source is required");
        }
        this.sources = List.of(sources);
    }

    @Override
    public String name() {
        String used = lastUsed;
        if (used != null) {
            return used;
        }
        return String.join("|", sources.stream().map(PriceSource::name).toList());
    }

    @Override
    public T fetch() {
        List<String> failures = new ArrayList<>();
        for (PriceSource<T> source : sources) {
            try {
                T quote = Objects.requireNonNull(source.fetch(), source.name() + " returned no quote");
                lastUsed = source.name();
                if (!failures.isEmpty()) {
                    log.warn("Used fallback {} after: {}", source.name(), String.join("; ", failures));
                }
                return quote;
            } catch (PriceSourceException | NullPointerException e) {
                failures.add(source.name() + ": " + e.getMessage());
            }
        }
        lastUsed = null;
        throw new PriceSourceException("All sources failed: " + String.join("; ", failures));
    }
}
