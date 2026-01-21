package com.fivontwov.dto;

import com.fivontwov.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentWithUserDTO {
    private Long id;
    private Long topicId;
    private Long parentCommentId;
    private Long userId;
    private String body;
    private Instant createdAt;
    private UserDTO creator;

    public static CommentWithUserDTO fromComment(Comment comment, UserDTO creator) {
        CommentWithUserDTO dto = new CommentWithUserDTO();
        dto.setId(comment.getId());
        dto.setTopicId(comment.getTopicId());
        dto.setParentCommentId(comment.getParentCommentId());
        dto.setUserId(comment.getUserId());
        dto.setBody(comment.getBody());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setCreator(creator);
        return dto;
    }
}
