package com.playerslog.backend.user.service;

import com.playerslog.backend.goll.repository.GollRepository;
import com.playerslog.backend.user.domain.User;
import com.playerslog.backend.user.dto.UpdateProfileRequest;
import com.playerslog.backend.user.dto.UserProfileResponse;
import com.playerslog.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GollRepository gollRepository;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        long createdGollsCount = gollRepository.countByOwnerId(userId);
        
        UserProfileResponse.StatsDto stats = UserProfileResponse.StatsDto.builder()
                .created(createdGollsCount)
                .liked(0)
                .cheers(0)
                .build();

        return UserProfileResponse.from(user, stats);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        user.updateProfile(request.name(), request.description(), request.socialLinks());

        // Note: Spring Data JPA's save is not strictly necessary here in a transactional context
        // as the changes to the managed entity will be flushed automatically.
        // However, explicitly calling it can make the intent clearer.
        User updatedUser = userRepository.save(user);

        // Recalculate stats for the response
        return getUserProfile(updatedUser.getId());
    }
}
