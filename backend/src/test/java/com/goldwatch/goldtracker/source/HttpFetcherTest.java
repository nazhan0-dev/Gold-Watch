package com.goldwatch.goldtracker.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.EOFException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

class HttpFetcherTest {

    @Test
    void describeShowsRootCauseWhenMessageIsNull() {
        var error = new ResourceAccessException("I/O error on GET request: null",
                new IOException(null, new EOFException()));

        assertThat(HttpFetcher.describe(error))
                .isEqualTo("I/O error on GET request: null <- java.io.EOFException: null");
    }

    @Test
    void describeWithoutCauseIsJustTheMessage() {
        assertThat(HttpFetcher.describe(new IllegalStateException("boom"))).isEqualTo("boom");
    }
}
