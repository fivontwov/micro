package com.fivontwov.dto;

import com.fivontwov.model.Topic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicWithUserDTO {
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private Instant createdAt;
    private UserDTO creator;

    public static TopicWithUserDTO fromTopic(Topic topic, UserDTO creator) {
        TopicWithUserDTO dto = new TopicWithUserDTO();
        dto.setId(topic.getId());
        dto.setUserId(topic.getUserId());
        dto.setTitle(topic.getTitle());
        dto.setBody(topic.getBody());
        dto.setCreatedAt(topic.getCreatedAt());
        dto.setCreator(creator);
        return dto;
    }
}
