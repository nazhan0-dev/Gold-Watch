package com.goldwatch.goldtracker.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goldwatch.goldtracker.api.dto.AnnouncementsResponse;
import com.goldwatch.goldtracker.api.dto.HistoryResponse;
import com.goldwatch.goldtracker.api.dto.LatestPricesResponse;
import com.goldwatch.goldtracker.api.dto.ThaiGoldLatest;
import com.goldwatch.goldtracker.config.WebConfig;
import com.goldwatch.goldtracker.domain.HistoryRange;
import com.goldwatch.goldtracker.domain.PriceSeries;
import com.goldwatch.goldtracker.service.PriceQueryService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PriceController.class)
@Import(WebConfig.class)
class PriceControllerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 22);

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PriceQueryService prices;

    @BeforeEach
    void today() {
        when(prices.today()).thenReturn(TODAY);
    }

    @Test
    void latestReturnsExactDecimalStringsAndCacheHeader() throws Exception {
        when(prices.latest()).thenReturn(new LatestPricesResponse(
                new ThaiGoldLatest("56950.00", "57150.00", "57000.00", "150.00", "0.26",
                        Instant.parse("2026-11-22T02:09:00Z")),
                null, null, null, Instant.parse("2026-11-22T02:09:00Z")));

        mvc.perform(get("/api/prices/latest"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=30, public"))
                .andExpect(jsonPath("$.bar.sell").value("57150.00"))
                .andExpect(jsonPath("$.bar.updatedAt").value("2026-11-22T02:09:00Z"))
                .andExpect(jsonPath("$.ornament").isEmpty());
    }

    @Test
    void historyAcceptsCaseInsensitiveCodes() throws Exception {
        when(prices.history(PriceSeries.SPOT, HistoryRange.ONE_WEEK)).thenReturn(new HistoryResponse(
                "spot", "1W", "day", "USD", Instant.EPOCH, Instant.EPOCH, List.of(), null));

        mvc.perform(get("/api/prices/history").param("type", "SPOT").param("range", "1w"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value("1W"))
                .andExpect(jsonPath("$.points").isArray());
    }

    @Test
    void unknownRangeIsA400ProblemWithAllowedValues() throws Exception {
        mvc.perform(get("/api/prices/history").param("type", "bar").param("range", "5Y"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.parameter").value("range"))
                .andExpect(jsonPath("$.detail").value("Unknown range '5Y'. Allowed values: 1D, 1W, 1M, 1Y"));
        verifyNoInteractions(prices);
    }

    @Test
    void unknownTypeIsA400() throws Exception {
        mvc.perform(get("/api/prices/history").param("type", "silver").param("range", "1D"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parameter").value("type"));
    }

    @Test
    void missingParameterIsA400() throws Exception {
        mvc.perform(get("/api/prices/history").param("type", "bar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void announcementsDefaultToThaiToday() throws Exception {
        when(prices.announcements(TODAY)).thenReturn(new AnnouncementsResponse(TODAY, List.of()));

        mvc.perform(get("/api/prices/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-22"));
        verify(prices).announcements(TODAY);
    }

    @Test
    void malformedAndFutureDatesAre400() throws Exception {
        mvc.perform(get("/api/prices/announcements").param("date", "22-11-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid date '22-11-2026'. Expected format YYYY-MM-DD"));
        mvc.perform(get("/api/prices/announcements").param("date", "2026-11-23"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parameter").value("date"));
    }

    @Test
    void corsAllowsOnlyConfiguredOrigin() throws Exception {
        when(prices.latest()).thenReturn(new LatestPricesResponse(null, null, null, null, null));

        mvc.perform(get("/api/prices/latest").header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        mvc.perform(get("/api/prices/latest").header("Origin", "https://evil.example"))
                .andExpect(status().isForbidden());
        mvc.perform(options("/api/prices/latest")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unexpectedErrorsAreJsonToo() throws Exception {
        when(prices.history(any(), any())).thenThrow(new IllegalStateException("db down"));

        mvc.perform(get("/api/prices/history").param("type", "bar").param("range", "1D"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal error"));
    }
}
