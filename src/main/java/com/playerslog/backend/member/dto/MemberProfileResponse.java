package com.playerslog.backend.member.dto;

import com.playerslog.backend.auth.domain.SocialLinks;
import com.playerslog.backend.member.entity.Member;

public record MemberProfileResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String description,
        SocialLinks socialLinks
) {
    public static MemberProfileResponse from(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getDescription(),
                member.getSocialLinks()
        );
    }
}
