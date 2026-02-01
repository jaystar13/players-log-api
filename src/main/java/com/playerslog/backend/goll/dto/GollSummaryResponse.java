package com.playerslog.backend.goll.dto;

import com.playerslog.backend.goll.domain.Goll;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class GollSummaryResponse {
    private Long id;
    private String title;
    private String sport;
    private LocalDateTime matchDate;
    private String venue;
    private OwnerResponse owner;
    private Set<String> previewLinks;
    private String description;
    private Set<ParticipantResponse> participants;
    private long likes;
    private boolean isLiked;

    public static GollSummaryResponse fromEntity(Goll goll, long likeCount, boolean isLiked) {
        return GollSummaryResponse.builder()
                .id(goll.getId())
                .title(goll.getTitle())
                .sport(goll.getSport())
                .matchDate(goll.getMatchDate())
                .venue(goll.getVenue())
                .description(goll.getDescription())
                .owner(OwnerResponse.builder()
                        .name(goll.getOwner().getName())
                        .profileImageUrl(goll.getOwner().getProfileImageUrl())
                        .description(goll.getOwner().getDescription())
                        .build())
                .previewLinks(goll.getPreviewLinks())
                .participants(goll.getParticipants().stream()
                        .map(ParticipantResponse::fromEntity)
                        .collect(Collectors.toSet()))
                .likes(likeCount)
                .isLiked(isLiked)
                .build();
    }

    @Data
    @Builder
    public static class OwnerResponse {
        private String name;
        private String profileImageUrl;
        private String description;
    }
}
