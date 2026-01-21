package com.fivontwov.repo;

import com.fivontwov.model.TopicVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicVoteRepository extends JpaRepository<TopicVote, Long> {
    Optional<TopicVote> findByUserIdAndTopicId(Long userId, Long topicId);
    int countByTopicIdAndValue(Long topicId, int value);
}
