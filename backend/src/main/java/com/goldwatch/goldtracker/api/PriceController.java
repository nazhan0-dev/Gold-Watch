package com.goldwatch.goldtracker.api;

import com.goldwatch.goldtracker.api.dto.AnnouncementsResponse;
import com.goldwatch.goldtracker.api.dto.HistoryResponse;
import com.goldwatch.goldtracker.api.dto.LatestPricesResponse;
import com.goldwatch.goldtracker.domain.HistoryRange;
import com.goldwatch.goldtracker.domain.PriceSeries;
import com.goldwatch.goldtracker.service.PriceQueryService;
import java.time.Duration;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only price API. All timestamps are ISO-8601 UTC; prices are decimal strings. */
@RestController
@RequestMapping(path = "/api/prices", produces = "application/json")
public class PriceController {

    private static final LocalDate EARLIEST_DATE = LocalDate.of(2000, 1, 1);

    private final PriceQueryService prices;

    public PriceController(PriceQueryService prices) {
        this.prices = prices;
    }

    @GetMapping("/latest")
    public ResponseEntity<LatestPricesResponse> latest() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)).cachePublic())
                .body(prices.latest());
    }

    @GetMapping("/history")
    public HistoryResponse history(@RequestParam("type") PriceSeries type, @RequestParam("range") HistoryRange range) {
        return prices.history(type, range);
    }

    @GetMapping("/announcements")
    public AnnouncementsResponse announcements(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate today = prices.today();
        LocalDate day = date == null ? today : date;
        if (day.isAfter(today)) {
            throw new InvalidParameterException("date", "date must not be in the future (today in Thailand is " + today + ")");
        }
        if (day.isBefore(EARLIEST_DATE)) {
            throw new InvalidParameterException("date", "date must be on or after " + EARLIEST_DATE);
        }
        return prices.announcements(day);
    }
}
