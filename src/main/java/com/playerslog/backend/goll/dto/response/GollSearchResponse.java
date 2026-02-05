package com.playerslog.backend.goll.dto.response;

import com.playerslog.backend.goll.domain.Goll;
import java.time.LocalDateTime;

public record GollSearchResponse(
        Long id,
        String title,
        String sport,
        LocalDateTime matchDate,
        String ownerName,
        String venue
) {
    public static GollSearchResponse from(Goll goll) {
        return new GollSearchResponse(
                goll.getId(),
                goll.getTitle(),
                goll.getSport(),
                goll.getMatchDate(),
                goll.getOwner() != null ? goll.getOwner().getName() : "Unknown",
                goll.getVenue()
        );
    }
}
