package com.playerslog.backend.goll.dto;

import java.util.Map;

public record LikeUpdateEvent(
        String type,
        Long gollId,
        Long likes,
        Map<Long, Integer> voteCounts
) {
    public static LikeUpdateEvent gollLikeUpdate(Long gollId, Long likes, Map<Long, Integer> voteCounts) {
        return new LikeUpdateEvent("GOLL_LIKE_UPDATE", gollId, likes, voteCounts);
    }

    public static LikeUpdateEvent voteUpdate(Long gollId, Long likes, Map<Long, Integer> voteCounts) {
        return new LikeUpdateEvent("VOTE_UPDATE", gollId, likes, voteCounts);
    }
}
