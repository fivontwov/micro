package com.micro.notification.kafka;

import com.micro.notification.event.CommentCreatedEvent;
import com.micro.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer để lắng nghe events từ topic forum.comment.created
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
        topics = "forum.comment.created",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCommentCreatedEvent(CommentCreatedEvent event) {
        log.info("Received comment event from Kafka: commentId={}, topicId={}, commenterId={}", 
                event.getCommentId(), event.getTopicId(), event.getCommenterId());
        
        try {
            sendNotifications(event);
            
            log.info("Successfully processed comment event: commentId={}", event.getCommentId());
        } catch (Exception e) {
            log.error("Failed to process comment event: commentId={}", event.getCommentId(), e);
            // TODO: Implement retry logic or dead letter queue
        }
    }

    private void sendNotifications(CommentCreatedEvent event) {
        // 1. Notify topic creator (nếu không phải chính họ comment)
        if (!event.getCommenterId().equals(event.getTopicCreatorId())) {
            if (event.getTopicCreatorEmail() != null) {
                log.info("Sending notification to topic creator: {}", event.getTopicCreatorEmail());
                emailService.sendCommentNotification(
                    event.getTopicCreatorEmail(),
                    event.getCommenterName(),
                    event.getTopicTitle(),
                    event.getCommentBody(),
                    false // not a reply notification
                );
            }
        }

        // 2. Notify parent comment creator (nếu đây là reply và không phải chính họ)
        if (event.getParentCommentId() != null 
            && event.getParentCommentCreatorEmail() != null
            && !event.getCommenterId().equals(event.getParentCommentCreatorId())) {
            
            // Avoid duplicate: không gửi cho parent comment creator nếu họ cũng là topic creator
            if (!event.getParentCommentCreatorId().equals(event.getTopicCreatorId())) {
                log.info("Sending reply notification to parent comment creator: {}", 
                        event.getParentCommentCreatorEmail());
                emailService.sendCommentNotification(
                    event.getParentCommentCreatorEmail(),
                    event.getCommenterName(),
                    event.getTopicTitle(),
                    event.getCommentBody(),
                    true // is a reply notification
                );
            }
        }
    }
}
