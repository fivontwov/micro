package com.micro.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event được gửi từ Forum Service khi có comment mới
 * Consumer: Notification Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEvent {
    private Long commentId;
    private Long topicId;
    private Long commenterId;
    private String commenterEmail;
    private String commenterName;

    private Long topicCreatorId;
    private String topicCreatorEmail;

    private Long parentCommentId;
    private Long parentCommentCreatorId;
    private String parentCommentCreatorEmail;

    private String commentBody;
    private LocalDateTime createdAt;
    private String topicTitle;
}
