package com.goldwatch.goldtracker.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response of GET /api/prices/history. {@code interval} is hour, day or week; {@code stats} is null when the
 * range has no data yet.
 */
public record HistoryResponse(
        String type,
        String range,
        String interval,
        String unit,
        Instant from,
        Instant to,
        List<HistoryPoint> points,
        HistoryStats stats) {
}
