package com.goldwatch.goldtracker.source;

/**
 * A single upstream data provider. Each provider lives in its own implementation, so replacing or
 * adding a provider only means registering a different bean of the same quote type.
 *
 * @param <T> the quote type this source produces ({@link ThaiGoldQuote}, {@link SpotQuote}, {@link FxQuote})
 */
public interface PriceSource<T> {

    /** Short identifier stored in the {@code source} column, e.g. {@code gold-api.com}. */
    String name();

    /**
     * Fetches and validates the current quote.
     *
     * @throws PriceSourceException when the provider is unreachable or returns data that cannot be trusted
     */
    T fetch();
}
