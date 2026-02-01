package com.playerslog.backend.user.dto;

import com.playerslog.backend.user.domain.SocialLinks;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50)
        String name,
        @Size(max = 200)
        String description,
        SocialLinks socialLinks
) {
}
