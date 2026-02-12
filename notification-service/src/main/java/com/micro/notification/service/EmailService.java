package com.micro.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Gửi email thông báo khi có comment mới
     * 
     * @param recipientEmail Email của người nhận
     * @param commenterName Tên người comment
     * @param topicTitle Tiêu đề topic
     * @param commentBody Nội dung comment
     * @param isReply true nếu đây là reply, false nếu comment trực tiếp
     */
    public void sendCommentNotification(
        String recipientEmail,
        String commenterName,
        String topicTitle,
        String commentBody,
        boolean isReply
    ) {
        try {
            log.info("Preparing email to: {}, commenter: {}, topic: {}", 
                    recipientEmail, commenterName, topicTitle);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Email metadata
            helper.setTo(recipientEmail);
            helper.setFrom("studyapp@forumapp.com");
            helper.setSubject(buildSubject(commenterName, topicTitle, isReply));

            // Build HTML content from template
            String htmlContent = buildHtmlContent(commenterName, topicTitle, commentBody, isReply);
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);
            
            log.info("Successfully sent email to: {}", recipientEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", recipientEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildSubject(String commenterName, String topicTitle, boolean isReply) {
        if (isReply) {
            return String.format("%s replied to your comment on \"%s\"", commenterName, topicTitle);
        } else {
            return String.format("%s commented on your topic \"%s\"", commenterName, topicTitle);
        }
    }

    private String buildHtmlContent(
        String commenterName,
        String topicTitle,
        String commentBody,
        boolean isReply
    ) {
        Context context = new Context();
        context.setVariable("commenterName", commenterName);
        context.setVariable("topicTitle", topicTitle);
        context.setVariable("commentBody", commentBody);
        context.setVariable("isReply", isReply);
        context.setVariable("year", java.time.Year.now().getValue());

        return templateEngine.process("comment-notification", context);
    }
}
