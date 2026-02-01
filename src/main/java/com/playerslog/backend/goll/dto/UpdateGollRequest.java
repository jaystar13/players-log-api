package com.playerslog.backend.goll.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpdateGollRequest(
        @NotBlank String title,
        @NotBlank String sport,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime time,
        @NotBlank String venue,
        String description,
        @NotBlank String matchType,
        @NotBlank String participantUnit,
        @NotEmpty List<ParticipantRequest> participants,
        List<String> previewLinks
) {
    public record ParticipantRequest(
            Long id, // ID might be present for existing participants
            @NotBlank String name,
            @NotBlank String type,
            @NotNull Integer displayOrder
    ) {}
}
