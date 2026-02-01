package com.playerslog.backend.goll.dto.response;

import com.playerslog.backend.goll.domain.Goll;
import com.playerslog.backend.goll.domain.Participant;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record GollDetailResponse(
        Long id,
        String title,
        String sport,
        LocalDateTime matchDate,
        String venue,
        String description,
        String matchType,
        String participantUnit,
        OwnerDto owner,
        List<ParticipantDto> participants,
        Set<String> previewLinks,
        long likes,
        boolean isLiked,
        String userVoteId
) {
    public static GollDetailResponse of(Goll goll, long likeCount, boolean isLiked, Map<Long, Integer> voteCounts, String userVoteId) {
        List<ParticipantDto> sortedParticipants = goll.getParticipants().stream()
                .map(p -> ParticipantDto.from(
                        p,
                        voteCounts.getOrDefault(p.getId(), 0),
                        Objects.equals(String.valueOf(p.getId()), userVoteId)
                ))
                .sorted(Comparator.comparing(ParticipantDto::order))
                .collect(Collectors.toList());

        return GollDetailResponse.builder()
                .id(goll.getId())
                .title(goll.getTitle())
                .sport(goll.getSport())
                .matchDate(goll.getMatchDate())
                .venue(goll.getVenue())
                .description(goll.getDescription())
                .matchType(goll.getMatchType())
                .participantUnit(goll.getParticipantUnit())
                .owner(OwnerDto.from(goll.getOwner()))
                .participants(sortedParticipants)
                .previewLinks(goll.getPreviewLinks())
                .likes(likeCount)
                .isLiked(isLiked)
                .userVoteId(userVoteId)
                .build();
    }

    @Builder
    public record ParticipantDto(
            Long id,
            String name,
            Integer order,
            Integer votes,
            boolean isVotedByUser
    ) {
        public static ParticipantDto from(Participant participant, Integer votes, boolean isVotedByUser) {
            return ParticipantDto.builder()
                    .id(participant.getId())
                    .name(participant.getName())
                    .order(participant.getDisplayOrder())
                    .votes(votes)
                    .isVotedByUser(isVotedByUser)
                    .build();
        }
    }
}
