package com.fivontwov.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "topic_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // -1 or 1
    private Integer value;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
