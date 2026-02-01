package com.playerslog.backend.user.dto;

import com.playerslog.backend.auth.domain.Role;
import com.playerslog.backend.user.domain.SocialLinks;
import com.playerslog.backend.user.domain.User;
import lombok.Builder;

@Builder
public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String profileImageUrl,
        String description,
        Role role,
        StatsDto stats,
        SocialLinks socialLinks
) {
    @Builder
    public record StatsDto(
            long created,
            long liked,
            long cheers
    ) {}

    public static UserProfileResponse from(User user, StatsDto stats) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .description(user.getDescription())
                .role(user.getRole())
                .stats(stats)
                .socialLinks(user.getSocialLinks())
                .build();
    }
}
