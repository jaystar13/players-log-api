package com.playerslog.backend.goll.dto.response;

import com.playerslog.backend.user.domain.User;
import lombok.Builder;

@Builder
public record OwnerDto(
        Long id,
        String name,
        String profileImageUrl,
        String description
) {
    public static OwnerDto from(User owner) {
        return OwnerDto.builder()
                .id(owner.getId())
                .name(owner.getName())
                .profileImageUrl(owner.getProfileImageUrl())
                .description(owner.getDescription())
                .build();
    }
}
