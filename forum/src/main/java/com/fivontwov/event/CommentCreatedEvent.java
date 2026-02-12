package com.fivontwov.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


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
    
    /**
     * tt của parent comment (nếu đây là reply)
     * null nếu comment trực tiếp vào topic
     */
    private Long parentCommentId;
    private Long parentCommentCreatorId;
    private String parentCommentCreatorEmail;
    private String commentBody;
    private LocalDateTime createdAt;
    private String topicTitle;
}
