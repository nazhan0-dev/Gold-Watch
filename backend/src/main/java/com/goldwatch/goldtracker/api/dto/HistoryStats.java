package com.goldwatch.goldtracker.api.dto;

/** Summary over the whole requested range. */
public record HistoryStats(String open, String close, String high, String low, String change, String changePercent) {
}
