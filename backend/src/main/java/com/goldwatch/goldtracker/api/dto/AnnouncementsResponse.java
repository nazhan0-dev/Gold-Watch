package com.goldwatch.goldtracker.api.dto;

import java.time.LocalDate;
import java.util.List;

/** Response of GET /api/prices/announcements; rounds are ordered newest first. */
public record AnnouncementsResponse(LocalDate date, List<AnnouncementRound> rounds) {
}
