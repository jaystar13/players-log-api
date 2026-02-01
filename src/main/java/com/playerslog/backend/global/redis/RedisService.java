package com.playerslog.backend.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // --- Key Generation ---
    private String getGollLikersKey(Long gollId) {
        return "goll:" + gollId + ":likers";
    }

    private String getGollVoteCountsKey(Long gollId) {
        return "goll:" + gollId + ":vote_counts";
    }

    private String getGollUserVotesKey(Long gollId) {
        return "goll:" + gollId + ":user_votes";
    }

    // --- Goll Like Operations ---
    public boolean toggleGollLike(Long gollId, String userId) {
        String key = getGollLikersKey(gollId);
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userId))) {
            redisTemplate.opsForSet().remove(key, userId);
            return false; // Like removed
        } else {
            redisTemplate.opsForSet().add(key, userId);
            return true; // Like added
        }
    }

    public long getGollLikeCount(Long gollId) {
        Long count = redisTemplate.opsForSet().size(getGollLikersKey(gollId));
        return count != null ? count : 0L;
    }

    public boolean isGollLikedByUser(Long gollId, String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(getGollLikersKey(gollId), userId));
    }

    // --- Participant Vote Operations ---
    public String toggleParticipantVote(Long gollId, String participantId, String userId) {
        String userVotesKey = getGollUserVotesKey(gollId);
        String voteCountsKey = getGollVoteCountsKey(gollId);

        Object existingVote = redisTemplate.opsForHash().get(userVotesKey, userId);

        // Case 1: User has voted before in this goll.
        if (existingVote != null) {
            String oldParticipantId = existingVote.toString();

            // Case 1a: UN-VOTE (clicking the same person)
            if (oldParticipantId.equals(participantId)) {
                redisTemplate.opsForHash().delete(userVotesKey, userId);
                long newCount = redisTemplate.opsForHash().increment(voteCountsKey, participantId, -1);
                if (newCount <= 0) {
                    redisTemplate.opsForHash().delete(voteCountsKey, participantId);
                }
                return null; // Vote removed
            }
            
            // Case 1b: CHANGE VOTE (clicking a new person)
            else {
                // Decrement old participant's count
                long oldNewCount = redisTemplate.opsForHash().increment(voteCountsKey, oldParticipantId, -1);
                if (oldNewCount <= 0) {
                    redisTemplate.opsForHash().delete(voteCountsKey, oldParticipantId);
                }
                // Increment new participant's count
                redisTemplate.opsForHash().increment(voteCountsKey, participantId, 1);
                // Update user's choice
                redisTemplate.opsForHash().put(userVotesKey, userId, participantId);
                return participantId; // Vote changed
            }
        } 
        
        // Case 2: NEW VOTE (voting for the first time in this goll)
        else {
            redisTemplate.opsForHash().increment(voteCountsKey, participantId, 1);
            redisTemplate.opsForHash().put(userVotesKey, userId, participantId);
            return participantId; // Vote added
        }
    }

    public Map<Long, Integer> getParticipantVoteCounts(Long gollId) {
        String voteCountsKey = getGollVoteCountsKey(gollId);
        return redisTemplate.<String, Object>opsForHash().entries(voteCountsKey).entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey()),
                        e -> Integer.parseInt(String.valueOf(e.getValue()))
                ));
    }

    public String getUserVoteForGoll(Long gollId, String userId) {
        Object votedParticipant = redisTemplate.opsForHash().get(getGollUserVotesKey(gollId), userId);
        return votedParticipant != null ? votedParticipant.toString() : null;
    }
}
