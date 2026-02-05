package com.playerslog.backend.user.service;

import com.playerslog.backend.global.redis.RedisService;
import com.playerslog.backend.goll.dto.GollSummaryResponse;
import com.playerslog.backend.goll.repository.GollRepository;
import com.playerslog.backend.user.domain.User;
import com.playerslog.backend.user.dto.UpdateProfileRequest;
import com.playerslog.backend.user.dto.UserProfileResponse;
import com.playerslog.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GollRepository gollRepository;
    private final RedisService redisService;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        long createdGollsCount = gollRepository.countByOwnerId(userId);
        long likedGollsCount = redisService.getLikedGollsForUser(String.valueOf(userId)).size();

        UserProfileResponse.StatsDto stats = UserProfileResponse.StatsDto.builder()
                .created(createdGollsCount)
                .liked(likedGollsCount)
                .cheers(0) // TODO: Implement cheer count
                .build();

        return UserProfileResponse.from(user, stats);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        user.updateProfile(request.name(), request.description(), request.socialLinks());

        User updatedUser = userRepository.save(user);

        return getUserProfile(updatedUser.getId());
    }

    public Page<GollSummaryResponse> getGollsForUser(Long userId, String type, Pageable pageable, Long currentUserId) {
        Page<com.playerslog.backend.goll.domain.Goll> golls;

        if ("created".equals(type)) {
            golls = gollRepository.findByOwnerId(userId, pageable);
        } else if ("liked".equals(type)) {
            Set<String> likedGollIdsStr = redisService.getLikedGollsForUser(String.valueOf(userId));
            if (likedGollIdsStr.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }
            Set<Long> likedGollIds = likedGollIdsStr.stream().map(Long::parseLong).collect(Collectors.toSet());
            // This fetches all liked golls, pagination is not properly applied here on the DB query
            // For a large number of liked golls, this could be inefficient.
            // A possible improvement is to fetch only the IDs for the current page.
            golls = new PageImpl<>(gollRepository.findByIdIn(likedGollIds), pageable, likedGollIds.size());
        } else {
            return Page.empty(pageable);
        }

        return golls.map(goll -> {
            // *** MANUAL INITIALIZATION FIX ***
            // By calling .size(), we force Hibernate to execute the query for these lazy collections
            // while the transaction is still active.
            goll.getPreviewLinks().size();
            goll.getParticipants().size();

            long likeCount = redisService.getGollLikeCount(goll.getId());
            boolean isLiked = currentUserId != null && redisService.isGollLikedByUser(goll.getId(), String.valueOf(currentUserId));
            return GollSummaryResponse.fromEntity(goll, likeCount, isLiked);
        });
    }
}
