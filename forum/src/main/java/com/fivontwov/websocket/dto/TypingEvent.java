package com.fivontwov.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event được gửi qua WebSocket khi user đang gõ / dừng gõ
 *
 * Client gửi lên: { "userId": 1, "userName": "Alice", "action": "TYPING" }
 * Server broadcast: thêm topicId + timestamp rồi push xuống tất cả subscriber
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {

    public enum Action {
        TYPING,   // Đang gõ
        STOPPED   // Dừng gõ (hoặc submit)
    }

    private Long userId;

    private String userName;

    private Long topicId;

    /** TYPING hoặc STOPPED */
    private Action action;

    private LocalDateTime timestamp;
}
