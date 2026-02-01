package com.playerslog.backend.goll.dto.response;

import com.playerslog.backend.goll.domain.Participant;
import lombok.Builder;

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
