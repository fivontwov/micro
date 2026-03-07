package com.fivontwov.websocket.controller;

import com.fivontwov.websocket.dto.TypingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * Xử lý WebSocket messages từ client (Client → Server → Broadcast)
 *
 * Client gửi tới : /app/forum/{topicId}/typing
 * Server broadcast: /topic/forum/{topicId}/typing
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TopicWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Nhận typing event từ client và broadcast cho tất cả người xem cùng topic
     *
     * @param topicId  ID của topic (từ URL path)
     * @param event    TypingEvent từ client: { userId, userName, action }
     */
    @MessageMapping("/forum/{topicId}/typing")
    public void handleTyping(
            @DestinationVariable Long topicId,
            @Payload TypingEvent event
    ) {
        // Server bổ sung topicId và timestamp
        event.setTopicId(topicId);
        event.setTimestamp(LocalDateTime.now());

        log.info("[WebSocket] Typing event: topicId={}, user={}, action={}",
                topicId, event.getUserName(), event.getAction());

        // Broadcast tới tất cả client đang subscribe topic này
        messagingTemplate.convertAndSend(
                "/topic/forum/" + topicId + "/typing",
                event
        );
    }
}
