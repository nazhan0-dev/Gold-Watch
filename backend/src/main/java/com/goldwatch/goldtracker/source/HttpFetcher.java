package com.goldwatch.goldtracker.source;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Thin wrapper that turns any transport or HTTP status failure into a {@link PriceSourceException}. */
@Component
public class HttpFetcher {

    private final RestClient restClient;

    public HttpFetcher(RestClient priceSourceRestClient) {
        this.restClient = priceSourceRestClient;
    }

    public String get(String url, MediaType accept) {
        String body;
        try {
            body = restClient.get().uri(url).accept(accept).retrieve().body(String.class);
        } catch (RestClientException e) {
            throw new PriceSourceException("Request to " + url + " failed: " + describe(e), e);
        }
        if (body == null || body.isBlank()) {
            throw new PriceSourceException("Empty response from " + url);
        }
        return body;
    }

    /**
     * Message plus the root cause, e.g. {@code I/O error on GET ... <- java.io.EOFException: null}.
     * Network errors often have a null message, so the cause's type is what tells us what went wrong.
     */
    static String describe(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = String.valueOf(error.getMessage());
        return root == error ? message : message + " <- " + root.getClass().getName() + ": " + root.getMessage();
    }
}
