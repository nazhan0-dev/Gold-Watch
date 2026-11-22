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
            throw new PriceSourceException("Request to " + url + " failed: " + e.getMessage(), e);
        }
        if (body == null || body.isBlank()) {
            throw new PriceSourceException("Empty response from " + url);
        }
        return body;
    }
}
