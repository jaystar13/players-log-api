package com.playerslog.backend.goll.controller;

import com.playerslog.backend.goll.service.GollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/golls")
@RequiredArgsConstructor
public class VoteController {

    private final GollService gollService;

    @PostMapping("/{gollId}/participants/{participantId}/vote")
    public ResponseEntity<?> voteForParticipant(
            @PathVariable Long gollId,
            @PathVariable Long participantId,
            @AuthenticationPrincipal Long userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(gollService.voteForParticipant(gollId, participantId, userId));
    }
}
