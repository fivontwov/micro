package com.fivontwov.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event được gửi khi có comment mới được tạo
 * Event này sẽ được gửi qua Kafka để thông báo cho các services khác
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEvent {
    
    /**
     * ID của comment vừa được tạo
     */
    private Long commentId;
    
    /**
     * ID của topic mà comment thuộc về
     */
    private Long topicId;
    
    /**
     * ID của user tạo comment này
     */
    private Long commenterId;
    
    /**
     * Email của user tạo comment (để gửi thông báo)
     */
    private String commenterEmail;
    
    /**
     * Tên của user tạo comment
     */
    private String commenterName;
    
    /**
     * ID của topic creator (người cần nhận thông báo)
     */
    private Long topicCreatorId;
    
    /**
     * Email của topic creator
     */
    private String topicCreatorEmail;
    
    /**
     * ID của parent comment (nếu đây là reply)
     * null nếu comment trực tiếp vào topic
     */
    private Long parentCommentId;
    
    /**
     * ID của parent comment creator (người cần nhận thông báo nếu là reply)
     * null nếu không phải reply
     */
    private Long parentCommentCreatorId;
    
    /**
     * Email của parent comment creator
     * null nếu không phải reply
     */
    private String parentCommentCreatorEmail;
    
    /**
     * Nội dung comment
     */
    private String commentBody;
    
    /**
     * Thời gian tạo comment
     */
    private LocalDateTime createdAt;
    
    /**
     * Title của topic (để hiển thị trong thông báo)
     */
    private String topicTitle;
}
