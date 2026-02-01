package com.playerslog.backend.goll.dto;

import com.playerslog.backend.goll.domain.Participant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantResponse {
    private Long id;
    private String name;
    private String type;

    public static ParticipantResponse fromEntity(Participant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .name(participant.getName())
                .type(participant.getType())
                .build();
    }
}
