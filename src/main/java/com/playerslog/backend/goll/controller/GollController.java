package com.playerslog.backend.goll.controller;

import com.playerslog.backend.global.sse.SseEmitterService;
import com.playerslog.backend.goll.domain.Goll;
import com.playerslog.backend.goll.dto.CreateGollRequest;
import com.playerslog.backend.goll.dto.GollSummaryResponse;
import com.playerslog.backend.goll.dto.UpdateGollRequest;
import com.playerslog.backend.goll.dto.response.GollDetailResponse;
import com.playerslog.backend.goll.dto.response.GollSearchResponse;
import com.playerslog.backend.goll.service.GollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/golls")
@RequiredArgsConstructor
public class GollController {

    private final GollService gollService;
    private final SseEmitterService sseEmitterService;

    @PostMapping
    public ResponseEntity<?> createGoll(@Valid @RequestBody CreateGollRequest request,
                                        @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(gollService.createGoll(request, userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<GollSearchResponse>> searchGolls(
            @RequestParam("q") String query,
            @RequestParam(value = "scope", defaultValue = "title") String scope,
            @PageableDefault(size = 5) Pageable pageable) {
        Page<GollSearchResponse> results = gollService.searchGolls(query, scope, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping
    public ResponseEntity<Page<GollSummaryResponse>> getGolls(
            @PageableDefault(size = 20, sort = "createdAt,desc") Pageable pageable,
            @AuthenticationPrincipal Long userId) { // userId can be null
        Page<GollSummaryResponse> golls = gollService.getGolls(pageable, userId);
        return ResponseEntity.ok(golls);
    }

    @GetMapping("/{gollId}")
    public ResponseEntity<GollDetailResponse> getGollDetail(
            @PathVariable Long gollId,
            @AuthenticationPrincipal Long userId) { // userId can be null
        GollDetailResponse gollDetail = gollService.findGollDetailById(gollId, userId);
        return ResponseEntity.ok(gollDetail);
    }

    @GetMapping(value = "/{gollId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToGollUpdates(@PathVariable Long gollId) {
        return sseEmitterService.subscribe(gollId);
    }

    @PutMapping("/{gollId}")
    public ResponseEntity<Goll> updateGoll(
            @PathVariable Long gollId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateGollRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Goll updatedGoll = gollService.updateGoll(gollId, userId, request);
        return ResponseEntity.ok(updatedGoll);
    }

    @PostMapping("/{gollId}/like")
    public ResponseEntity<?> toggleLikeOnGoll(
            @PathVariable Long gollId,
            @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(gollService.toggleGollLike(gollId, userId));
    }

    @PatchMapping("/{gollId}")
    public ResponseEntity<GollDetailResponse> patchGoll(
            @PathVariable Long gollId,
            @AuthenticationPrincipal Long userId,
            @RequestBody com.playerslog.backend.goll.dto.PatchGollRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        GollDetailResponse updatedGoll = gollService.patchGoll(gollId, userId, request);
        return ResponseEntity.ok(updatedGoll);
    }
}
