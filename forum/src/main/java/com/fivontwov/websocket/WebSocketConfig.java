package com.fivontwov.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket STOMP cho Forum Service
 *
 * Endpoint kết nối : ws://localhost:{port}/ws  (hoặc qua Gateway: /api/forum/ws)
 *
 * Client SUBSCRIBE (nhận):
 *   /topic/forum/{topicId}/typing   ← typing events của 1 topic
 *
 * Client SEND (gửi lên server):
 *   /app/forum/{topicId}/typing     ← gửi trạng thái đang gõ
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker cho /topic/** và /queue/**
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix để phân biệt messages gửi TỪ client lên server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
