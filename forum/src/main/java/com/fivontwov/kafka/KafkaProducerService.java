package com.fivontwov.kafka;

import com.fivontwov.event.CommentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    
    // Topic name cho comment events
    public static final String COMMENT_CREATED_TOPIC = "forum.comment.created";
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCommentCreatedEvent(CommentCreatedEvent event) {
        log.info("Sending comment created event to Kafka: commentId={}, topicId={}", 
                event.getCommentId(), event.getTopicId());
        
        try {
            // Gửi event vào Kafka topic
            // Key = commentId (để Kafka biết partition nào lưu message này)
            // Value = event object (sẽ được convert thành JSON tự động)
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                COMMENT_CREATED_TOPIC,
                event.getCommentId().toString(),
                event
            );
            
            // Log khi gửi thành công
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent comment event to Kafka: " +
                            "topic={}, partition={}, offset={}", 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send comment event to Kafka: commentId={}", 
                            event.getCommentId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error sending comment event to Kafka: commentId={}", 
                    event.getCommentId(), e);
        }
    }
}
