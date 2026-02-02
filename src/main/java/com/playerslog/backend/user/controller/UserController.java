package com.playerslog.backend.user.controller;

import com.playerslog.backend.user.dto.UpdateProfileRequest;
import com.playerslog.backend.user.dto.UserProfileResponse;
import com.playerslog.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileResponse userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileResponse updatedProfile = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/{userId}/golls")
    public ResponseEntity<org.springframework.data.domain.Page<com.playerslog.backend.goll.dto.GollSummaryResponse>> getGollsForUser(
            @PathVariable Long userId,
            @RequestParam(name = "type", defaultValue = "created") String type,
            @org.springframework.data.web.PageableDefault(size = 20, sort = "createdAt,desc") org.springframework.data.domain.Pageable pageable,
            @AuthenticationPrincipal Long currentUserId
    ) {
        org.springframework.data.domain.Page<com.playerslog.backend.goll.dto.GollSummaryResponse> golls = userService.getGollsForUser(userId, type, pageable, currentUserId);
        return ResponseEntity.ok(golls);
    }
}

