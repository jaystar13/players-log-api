package com.playerslog.backend.member.dto;

import com.playerslog.backend.auth.domain.SocialLinks;
import jakarta.validation.constraints.Size;

public record MemberUpdateDto(

        @Size(min = 2, max = 20)
        String nickname,

        @Size(max = 1000)
        String description,

        SocialLinks socialLinks
) {
}
