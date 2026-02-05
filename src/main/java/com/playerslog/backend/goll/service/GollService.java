package com.playerslog.backend.goll.service;

import com.playerslog.backend.global.redis.RedisService;
import com.playerslog.backend.global.sse.SseEmitterService;
import com.playerslog.backend.goll.domain.Goll;
import com.playerslog.backend.goll.domain.Participant;
import com.playerslog.backend.goll.dto.CreateGollRequest;
import com.playerslog.backend.goll.dto.GollSummaryResponse;
import com.playerslog.backend.goll.dto.LikeUpdateEvent;
import com.playerslog.backend.goll.dto.UpdateGollRequest;
import com.playerslog.backend.goll.dto.response.GollDetailResponse;
import com.playerslog.backend.goll.dto.response.GollSearchResponse;
import com.playerslog.backend.goll.repository.GollRepository;
import com.playerslog.backend.user.domain.User;
import com.playerslog.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GollService {

    private final GollRepository gollRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public Goll createGoll(CreateGollRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Goll goll = Goll.builder()
                .title(request.title())
                .sport(request.sport())
                .matchDate(LocalDateTime.of(request.date(), request.time()))
                .venue(request.venue())
                .description(request.description())
                .matchType(request.matchType())
                .participantUnit(request.participantUnit())
                .owner(owner)
                .build();

        Set<Participant> participants = request.participants().stream()
                .map(pDto -> Participant.builder()
                        .name(pDto.name())
                        .type(pDto.type())
                        .displayOrder(pDto.displayOrder())
                        .build())
                .collect(Collectors.toSet());

        goll.setParticipants(participants);
        goll.setPreviewLinks(new HashSet<>(request.previewLinks()));

        return gollRepository.save(goll);
    }

    public Page<GollSearchResponse> searchGolls(String query, String scope, Pageable pageable) {
        if (!StringUtils.hasText(query)) {
            return Page.empty();
        }

        // Currently, only 'title' scope is implemented as per the immediate requirement.
        if ("title".equalsIgnoreCase(scope)) {
            return gollRepository.searchByTitleFTS(query, pageable)
                    .map(GollSearchResponse::from);
        }
        
        return Page.empty();
    }

    public Page<GollSummaryResponse> getGolls(Pageable pageable, Long userId) {
        return gollRepository.findAllWithDetails(pageable)
                .map(goll -> {
                    long likeCount = redisService.getGollLikeCount(goll.getId());
                    boolean isLiked = userId != null && redisService.isGollLikedByUser(goll.getId(), String.valueOf(userId));
                    return GollSummaryResponse.fromEntity(goll, likeCount, isLiked);
                });
    }

    public GollDetailResponse findGollDetailById(Long gollId, Long userId) {
        Goll goll = gollRepository.findByIdWithDetails(gollId)
                .orElseThrow(() -> new NoSuchElementException("Goll not found with id: " + gollId));

        boolean isOwner = goll.getOwner() != null && goll.getOwner().getId().equals(userId);
        long likeCount = redisService.getGollLikeCount(gollId);
        boolean isLiked = userId != null && redisService.isGollLikedByUser(gollId, String.valueOf(userId));
        Map<Long, Integer> voteCounts = redisService.getParticipantVoteCounts(gollId);
        String userVote = userId != null ? redisService.getUserVoteForGoll(gollId, String.valueOf(userId)) : null;

        return GollDetailResponse.of(goll, likeCount, isLiked, voteCounts, userVote, isOwner);
    }

    @Transactional
    public Goll updateGoll(Long gollId, Long userId, UpdateGollRequest request) {
        Goll goll = gollRepository.findByIdWithDetails(gollId)
                .orElseThrow(() -> new NoSuchElementException("Goll not found with id: " + gollId));

        if (!goll.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("User does not have permission to update this goll.");
        }

        updateGollEntity(goll, request);
        return gollRepository.save(goll);
    }

    private void updateGollEntity(Goll goll, UpdateGollRequest request) {
        goll.update(
                request.title(),
                request.sport(),
                LocalDateTime.of(request.date(), request.time()),
                request.venue(),
                request.description(),
                request.matchType(),
                request.participantUnit()
        );

        Set<Participant> participants = request.participants().stream()
                .map(pDto -> Participant.builder()
                        .name(pDto.name())
                        .type(pDto.type())
                        .displayOrder(pDto.displayOrder())
                        .build())
                .collect(Collectors.toSet());

        goll.setParticipants(participants);
        goll.setPreviewLinks(new HashSet<>(request.previewLinks()));
    }

    @Transactional
    public Map<String, Long> toggleGollLike(Long gollId, Long userId) {
        boolean liked = redisService.toggleGollLike(gollId, String.valueOf(userId));
        long newLikeCount = redisService.getGollLikeCount(gollId);

        // Notify subscribers
        Map<Long, Integer> voteCounts = redisService.getParticipantVoteCounts(gollId);
        LikeUpdateEvent event = LikeUpdateEvent.gollLikeUpdate(gollId, newLikeCount, voteCounts);
        sseEmitterService.sendToGollSubscribers(gollId, event.type(), event);


        return Map.of("likes", newLikeCount, "liked", liked ? 1L : 0L);
    }

    @Transactional
    public Map<String, Object> voteForParticipant(Long gollId, Long participantId, Long userId) {
        String votedParticipantId = redisService.toggleParticipantVote(gollId, String.valueOf(participantId), String.valueOf(userId));
        Map<Long, Integer> voteCounts = redisService.getParticipantVoteCounts(gollId);

        // Notify subscribers
        long likeCount = redisService.getGollLikeCount(gollId);
        LikeUpdateEvent event = LikeUpdateEvent.voteUpdate(gollId, likeCount, voteCounts);
        sseEmitterService.sendToGollSubscribers(gollId, event.type(), event);

        Map<String, Object> response = new HashMap<>();
        response.put("votedParticipantId", votedParticipantId != null ? Long.parseLong(votedParticipantId) : null);
        response.put("voteCounts", voteCounts);
        return response;
    }

    @Transactional
    public GollDetailResponse patchGoll(Long gollId, Long userId, com.playerslog.backend.goll.dto.PatchGollRequest request) {
        Goll goll = gollRepository.findById(gollId)
                .orElseThrow(() -> new NoSuchElementException("Goll not found with id: " + gollId));

        if (!goll.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("User does not have permission to patch this goll.");
        }

        if (request.getStatus() != null) {
            goll.setStatus(request.getStatus());
        }

        gollRepository.save(goll);

        return findGollDetailById(gollId, userId);
    }
}