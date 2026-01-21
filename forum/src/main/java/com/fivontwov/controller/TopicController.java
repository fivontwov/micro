package com.fivontwov.controller;

import com.fivontwov.dto.*;
import com.fivontwov.model.Comment;
import com.fivontwov.model.Topic;
import com.fivontwov.repo.CommentRepository;
import com.fivontwov.repo.TopicRepository;
import com.fivontwov.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor

public class TopicController {

    private final TopicService topicService;
    private final TopicRepository topicRepository;
    private final CommentRepository commentRepository;

    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody CreateTopicRequest req) {
        Topic t = topicService.createTopic(req.getUserId(), req.getTitle(), req.getBody());
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @PostMapping("/{topicId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long topicId, @RequestBody AddCommentRequest req) {
        Comment c = topicService.addComment(topicId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @PostMapping("/{topicId}/votes")
    public ResponseEntity<Void> vote(@PathVariable Long topicId, @RequestBody VoteRequest req) {
        topicService.vote(topicId, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicWithUserDTO> getTopic(@PathVariable Long topicId) {
        return topicService.getTopicWithUser(topicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TopicWithUserDTO>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopicsWithUser());
    }

    @GetMapping("/{topicId}/comments")
    public ResponseEntity<List<CommentWithUserDTO>> listComments(@PathVariable Long topicId) {
        return ResponseEntity.ok(topicService.getCommentsWithUser(topicId));
    }

    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long topicId) {
        if (!topicRepository.existsById(topicId)) {
            return ResponseEntity.notFound().build();
        }
        topicRepository.deleteById(topicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{topicId}/comments/{commentId}")
    public ResponseEntity<CommentWithUserDTO> getComment(@PathVariable Long topicId, @PathVariable Long commentId) {
        return topicService.getCommentWithUser(commentId)
                .filter(c -> c.getTopicId().equals(topicId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{topicId}/comments/{commentId}")
    public ResponseEntity<Object> deleteComment(@PathVariable Long topicId, @PathVariable Long commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> c.getTopicId().equals(topicId))
                .map(c -> {
                    commentRepository.deleteById(commentId);
                    return ResponseEntity.<Void>noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
